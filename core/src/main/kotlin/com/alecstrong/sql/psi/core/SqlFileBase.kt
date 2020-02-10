package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.SqlCreateIndexStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlStmtList
import com.alecstrong.sql.psi.core.psi.SqlStmt
import com.alecstrong.sql.psi.core.psi.TableElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

abstract class SqlFileBase(
    viewProvider: FileViewProvider,
    language: Language
) : PsiFileBase(viewProvider, language) {
  private val symbolTable = SymbolTable()

  private var viewNames = setOf(*views().map { it.viewName.name }.toTypedArray())
  private var tableElements = setOf(*tables().toTypedArray())

  private val psiManager: PsiManager
    get() = PsiManager.getInstance(project)

  val sqlStmtList
    get() = findChildByClass(SqlStmtList::class.java)

  open fun tablesAvailable(sqlStmtElement: PsiElement): Collection<LazyQuery> {
    symbolTable.checkInitialized()
    val statement = (sqlStmtElement as SqlStmt).children.first()
    if (statement !is TableElement || statement is SqlCreateTableStmt) {
      return symbolTable.tables.values
    }
    return symbolTable.tables.filterKeys { it != statement }.values
  }

  open fun indexes(): List<SqlCreateIndexStmt> {
    val result = ArrayList<SqlCreateIndexStmt>()
    iterateSqlFiles { psiFile ->
      result.addAll(PsiTreeUtil.findChildrenOfType(psiFile, SqlCreateIndexStmt::class.java))
      return@iterateSqlFiles true
    }
    return result
  }

  open fun triggers(): List<SqlCreateTriggerStmt> {
    val result = ArrayList<SqlCreateTriggerStmt>()
    iterateSqlFiles { psiFile ->
      result.addAll(PsiTreeUtil.findChildrenOfType(psiFile, SqlCreateTriggerStmt::class.java))
      return@iterateSqlFiles true
    }
    return result
  }

  internal fun viewForName(name: String): SqlCreateViewStmt? {
    symbolTable.checkInitialized()
    return symbolTable.views[name]
  }

  private fun views(): List<SqlCreateViewStmt> {
    return sqlStmtList?.stmtList?.mapNotNull {
      it.createViewStmt
    }.orEmpty()
  }

  private fun tables(): List<TableElement> {
    return sqlStmtList?.stmtList?.mapNotNull {
      it.createViewStmt ?: it.createTableStmt ?: it.createVirtualTableStmt
    }.orEmpty()
  }

  override fun subtreeChanged() {
    super.subtreeChanged()
    if (parent == null) {
      // Lightweight copy of the original file. Dont do any mods.
      return
    }
    val newViews = views()
    val newTables = tables()
    iterateSqlFiles { psiFile ->
      if (psiFile !is SqlFileBase) return@iterateSqlFiles true

      viewNames.forEach { psiFile.symbolTable.views.remove(it)}
      tableElements.forEach { psiFile.symbolTable.tables.remove(it) }

      psiFile.symbolTable.views.putAll(newViews.map { it.viewName.name to it })
      psiFile.symbolTable.tables.putAll(newTables.map { it to it.tableExposed() })

      return@iterateSqlFiles true
    }

    viewNames = setOf(*newViews.map { it.viewName.name }.toTypedArray())
    tableElements = setOf(*newTables.toTypedArray())
  }

  protected open fun iterateSqlFiles(iterator: (PsiFile) -> Boolean) {
    ProjectRootManager.getInstance(project).fileIndex.iterateContent { file ->
      if (file.fileType != fileType) return@iterateContent true
      psiManager.findFile(file)?.let { psiFile ->
        return@iterateContent iterator(psiFile)
      }
      true
    }
  }

  /**
   * Creates a copy of this file keeping all external symbols intact.
   */
  fun copyWithSymbols(): SqlFileBase {
    val copy = copy() as SqlFileBase

    copy.symbolTable.views.putAll(symbolTable.views.filterKeys { it !in viewNames })
    copy.symbolTable.tables.putAll(symbolTable.tables.filterKeys { it !in tableElements })

    copy.symbolTable.views.putAll(copy.views().map { it.viewName.name to it })
    copy.symbolTable.tables.putAll(copy.tables().map { it to it.tableExposed() })

    copy.symbolTable.initialized = true
    return copy
  }

  inner class SymbolTable {
    internal val views = LinkedHashMap<String, SqlCreateViewStmt>()
    internal val tables = LinkedHashMap<TableElement, LazyQuery>()
    internal var initialized = false

    fun checkInitialized() {
      synchronized(initialized) {
        if (initialized) return

        iterateSqlFiles { psiFile ->
          if (psiFile !is SqlFileBase) return@iterateSqlFiles true
          psiFile.views().let {
            views.putAll(it.map { it.viewName.name to it })
          }
          psiFile.tables().let {
            tables.putAll(it.map { it to it.tableExposed() })
          }

          return@iterateSqlFiles true
        }

        initialized = true
      }
    }
  }
}