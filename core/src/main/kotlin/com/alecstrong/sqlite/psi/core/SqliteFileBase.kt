package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElement
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
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

  private val tables by lazy {
    val result = ArrayList<Pair<TableElement, LazyQuery>>()
    PsiTreeUtil.findChildrenOfType(this, TableElement::class.java).forEach { sqlStmt ->
      result.add(sqlStmt to sqlStmt.tableExposed())
    }
    return@lazy result
  }

  fun printAnalysis(printer: (String) -> Unit) {
    PsiTreeUtil.findChildrenOfType(this, SqliteCompositeElement::class.java).forEach { element ->
      if (element.analytics().isEmpty()) return@forEach
      printer("$element : ${element.analytics()}")
    }
  }

  open fun tablesAvailable(sqlStmtElement: PsiElement): List<LazyQuery> {
    val result = ArrayList<LazyQuery>()
    iterateSqliteFiles { psiFile ->
      if (psiFile is SqliteFileBase) {
        result.addAll(psiFile.tables
            .filter { it.first != sqlStmtElement }
            .map { it.second })
      }
      return@iterateSqliteFiles true
    }
    return result
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

  open fun views(): List<SqliteCreateViewStmt> {
    val result = ArrayList<SqliteCreateViewStmt>()
    iterateSqliteFiles { psiFile ->
      result.addAll(PsiTreeUtil.findChildrenOfType(psiFile, SqliteCreateViewStmt::class.java))
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