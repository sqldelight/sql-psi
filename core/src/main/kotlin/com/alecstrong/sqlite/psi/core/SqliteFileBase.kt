package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTableStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteSqlStmtList
import com.alecstrong.sqlite.psi.core.psi.SqliteStatement
import com.alecstrong.sqlite.psi.core.psi.TableElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

abstract class SqliteFileBase(
    viewProvider: FileViewProvider,
    language: Language
) : PsiFileBase(viewProvider, language) {
  private val symbolTable = SymbolTable()

  private var viewNames = setOf(*views().map { it.viewName.name }.toTypedArray())
  private var tableElements = setOf(*tables().toTypedArray())

  private val psiManager: PsiManager
    get() = PsiManager.getInstance(project)

  val sqlStmtList
    get() = findChildByClass(SqliteSqlStmtList::class.java)

  open fun tablesAvailable(sqlStmtElement: PsiElement): Collection<LazyQuery> {
    symbolTable.checkInitialized()
    val statement = (sqlStmtElement as SqliteStatement).sqlStmt.children.first()
    if (statement !is TableElement || statement is SqliteCreateTableStmt) {
      return symbolTable.tables.values
    }
    return symbolTable.tables.filterKeys { it != statement }.values
  }

  open fun indexes(): List<SqliteCreateIndexStmt> {
    val result = ArrayList<SqliteCreateIndexStmt>()
    iterateSqliteFiles { psiFile ->
      result.addAll(PsiTreeUtil.findChildrenOfType(psiFile, SqliteCreateIndexStmt::class.java))
      return@iterateSqliteFiles true
    }
    return result
  }

  open fun triggers(): List<SqliteCreateTriggerStmt> {
    val result = ArrayList<SqliteCreateTriggerStmt>()
    iterateSqliteFiles { psiFile ->
      result.addAll(PsiTreeUtil.findChildrenOfType(psiFile, SqliteCreateTriggerStmt::class.java))
      return@iterateSqliteFiles true
    }
    return result
  }

  internal fun viewForName(name: String): SqliteCreateViewStmt? {
    symbolTable.checkInitialized()
    return symbolTable.views[name]
  }

  private fun views(): List<SqliteCreateViewStmt> {
    return sqlStmtList?.statementList?.mapNotNull {
      it.sqlStmt.createViewStmt
    }.orEmpty()
  }

  private fun tables(): List<TableElement> {
    return sqlStmtList?.statementList?.mapNotNull {
      it.sqlStmt.createViewStmt ?: it.sqlStmt.createTableStmt ?: it.sqlStmt.createVirtualTableStmt
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
    iterateSqliteFiles { psiFile ->
      if (psiFile !is SqliteFileBase) return@iterateSqliteFiles true

      viewNames.forEach { psiFile.symbolTable.views.remove(it)}
      tableElements.forEach { psiFile.symbolTable.tables.remove(it) }

      psiFile.symbolTable.views.putAll(newViews.map { it.viewName.name to it })
      psiFile.symbolTable.tables.putAll(newTables.map { it to it.tableExposed() })

      return@iterateSqliteFiles true
    }

    viewNames = setOf(*newViews.map { it.viewName.name }.toTypedArray())
    tableElements = setOf(*newTables.toTypedArray())
  }

  protected open fun iterateSqliteFiles(iterator: (PsiFile) -> Boolean) {
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
  fun copyWithSymbols(): SqliteFileBase {
    val copy = copy() as SqliteFileBase

    copy.symbolTable.views.putAll(symbolTable.views.filterKeys { it !in viewNames })
    copy.symbolTable.tables.putAll(symbolTable.tables.filterKeys { it !in tableElements })

    copy.symbolTable.views.putAll(copy.views().map { it.viewName.name to it })
    copy.symbolTable.tables.putAll(copy.tables().map { it to it.tableExposed() })

    copy.symbolTable.initialized = true
    return copy
  }

  inner class SymbolTable {
    internal val views = LinkedHashMap<String, SqliteCreateViewStmt>()
    internal val tables = LinkedHashMap<TableElement, LazyQuery>()
    internal var initialized = false

    fun checkInitialized() {
      synchronized(initialized) {
        if (initialized) return

        iterateSqliteFiles { psiFile ->
          if (psiFile !is SqliteFileBase) return@iterateSqliteFiles true
          psiFile.views().let {
            views.putAll(it.map { it.viewName.name to it })
          }
          psiFile.tables().let {
            tables.putAll(it.map { it to it.tableExposed() })
          }

          return@iterateSqliteFiles true
        }

        initialized = true
      }
    }
  }
}