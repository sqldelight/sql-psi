package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteSqlStmt
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

  open fun tablesAvailable(sqlStmtElement: PsiElement): List<LazyQuery> {
    val result = ArrayList<LazyQuery>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        if (sqlStmt == sqlStmtElement) return@forEach
        sqlStmt.createTableStmt?.let { createTable ->
          result.add(LazyQuery(createTable.tableName) {
            createTable.compoundSelectStmt?.let {
              QueryResult(createTable.tableName, it.queryExposed().flatMap { it.columns })
            } ?: createTable.queryAvailable(this).single()
          })
        }
        sqlStmt.createViewStmt?.let { createView ->
          result.add(LazyQuery(createView.viewName) {
            QueryResult(createView.viewName,
                createView.compoundSelectStmt.queryExposed().flatMap { it.columns })
          })
        }
      }
      return@iterateSqliteFiles true
    }
    return result
  }

  open fun indexes(): List<SqliteCreateIndexStmt> {
    val result = ArrayList<SqliteCreateIndexStmt>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        sqlStmt.createIndexStmt?.let(result::add)
      }
      return@iterateSqliteFiles true
    }
    return result
  }

  open fun triggers(): List<SqliteCreateTriggerStmt> {
    val result = ArrayList<SqliteCreateTriggerStmt>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        sqlStmt.createTriggerStmt?.let(result::add)
      }
      return@iterateSqliteFiles true
    }
    return result
  }

  open fun views(): List<SqliteCreateViewStmt> {
    val result = ArrayList<SqliteCreateViewStmt>()
    iterateSqliteFiles { psiFile ->
      PsiTreeUtil.findChildrenOfType(psiFile, SqliteSqlStmt::class.java).forEach { sqlStmt ->
        sqlStmt.createViewStmt?.let(result::add)
      }
      return@iterateSqliteFiles true
    }
    return result
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
}