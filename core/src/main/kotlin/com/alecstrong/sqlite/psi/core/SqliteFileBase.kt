package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteSqlStmtList
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
  private val psiManager: PsiManager
    get() = PsiManager.getInstance(project)

  private val tables by ModifiableFileLazy(this) {
    val result = LinkedHashMap<TableElement, LazyQuery>()
    PsiTreeUtil.findChildrenOfType(this, TableElement::class.java).forEach { sqlStmt ->
      result.put(sqlStmt, sqlStmt.tableExposed())
    }
    return@ModifiableFileLazy result
  }

  private val otherTables by lazy {
    val result = ArrayList<LazyQuery>()
    iterateSqliteFiles { psiFile ->
      if (psiFile == this) return@iterateSqliteFiles true
      else if (psiFile is SqliteFileBase) result.addAll(psiFile.tables.values)
      return@iterateSqliteFiles true
    }
    return@lazy result
  }

  open fun tablesAvailable(sqlStmtElement: PsiElement): List<LazyQuery> {
    return otherTables + tables.filterKeys { it != sqlStmtElement }.values
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
    synchronized(FILE_UPDATED_AT) {
      if (FILE_UPDATED_AT.isEmpty()) {
        // Initialize the whole thing
        iterateSqliteFiles { psiFile ->
          if (psiFile !is SqliteFileBase) return@iterateSqliteFiles true
          val views = psiFile.views()
          VIEWS.putAll(views.map { it.viewName.name to it })
          VIEW_OWNERS.putAll(views.map { it.viewName.name to psiFile })
          FILE_UPDATED_AT.put(psiFile, psiFile.modificationStamp)
          return@iterateSqliteFiles true
        }
      }
      val owner = VIEW_OWNERS[name] ?: return null
      if (FILE_UPDATED_AT[owner]!! != owner.modificationStamp) {
        VIEW_OWNERS.filterValues { it == owner }.forEach { name, _ ->
          VIEWS.remove(name)
          VIEW_OWNERS.remove(name)
        }
        val views = owner.views()
        VIEWS.putAll(views.map { it.viewName.name to it })
        VIEW_OWNERS.putAll(views.map { it.viewName.name to owner })
        FILE_UPDATED_AT.put(owner, owner.modificationStamp)
      }
      return VIEWS[name]
    }
  }

  open fun views() = children.filterIsInstance<SqliteSqlStmtList>().single().statementList.mapNotNull {
    it.sqlStmt.createViewStmt
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

  companion object {
    private val VIEWS = LinkedHashMap<String, SqliteCreateViewStmt>()
    private val VIEW_OWNERS = LinkedHashMap<String, SqliteFileBase>()
    private val FILE_UPDATED_AT = LinkedHashMap<SqliteFileBase, Long>()
  }
}