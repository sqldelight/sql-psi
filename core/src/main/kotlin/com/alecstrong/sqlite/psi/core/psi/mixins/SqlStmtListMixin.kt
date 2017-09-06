package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElement.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
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

  override fun tablesAvailable(child: PsiElement): List<LazyQuery> {
    val result = ArrayList<LazyQuery>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        sqlStmt.createTableStmt?.let { createTable ->
          result.add(LazyQuery(createTable.tableName) {
            createTable.compoundSelectStmt?.let {
              QueryResult(createTable.tableName, it.queryExposed().flatMap { it.columns })
            } ?: createTable.queryAvailable(this).single()
          })
        }
        sqlStmt.createViewStmt?.let { createView ->
          result.add(LazyQuery(createView.viewName) {
            QueryResult(createView.viewName, createView.compoundSelectStmt.queryExposed().flatMap { it.columns })
          })
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

  internal fun views(): List<SqliteCreateViewStmt> {
    val result = ArrayList<SqliteCreateViewStmt>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        sqlStmt.createViewStmt?.let(result::add)
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