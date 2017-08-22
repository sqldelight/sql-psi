package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteSqlStmt
import com.intellij.lang.ASTNode
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

internal abstract class SqlStmtListMixin(node: ASTNode) : SqliteCompositeElementImpl(node) {
  private val psiManager: PsiManager
    get() = PsiManager.getInstance(project)

  override fun tablesAvailable(child: PsiElement): List<QueryResult> {
    val result = ArrayList<QueryResult>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        sqlStmt.createTableStmt?.let { createTable ->
          createTable.compoundSelectStmt?.let {
            result.add(QueryResult(createTable.tableName, it.queryExposed().flatMap { it.columns }))
          }
          if (createTable.columnDefList.isNotEmpty()) {
            result.addAll(createTable.queryAvailable(this))
          }
        }
        sqlStmt.createViewStmt?.let { createView ->
          result.add(QueryResult(createView.viewName, createView.compoundSelectStmt.queryExposed().flatMap { it.columns }))
        }
      }
      return@iterateSqliteFiles true
    }
    return result
  }

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    return emptyList()
  }

  internal fun indexes(): List<SqliteCreateIndexStmt> {
    val result = ArrayList<SqliteCreateIndexStmt>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        sqlStmt.createIndexStmt?.let(result::add)
      }
      return@iterateSqliteFiles true
    }
    return result
  }

  internal fun triggers(): List<SqliteCreateTriggerStmt> {
    val result = ArrayList<SqliteCreateTriggerStmt>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        sqlStmt.createTriggerStmt?.let(result::add)
      }
      return@iterateSqliteFiles true
    }
    return result
  }

  private fun iterateSqliteFiles(iterator: (PsiFile) -> Boolean) {
    val fileType = (parent as PsiFile).fileType
    ProjectRootManager.getInstance(project).fileIndex.iterateContent { file ->
      if (file.fileType != fileType) return@iterateContent true
      psiManager.findFile(file)?.let { psiFile ->
        return@iterateContent iterator(psiFile)
      }
      true
    }
  }
}