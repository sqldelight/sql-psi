package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteSqlStmt
import com.intellij.lang.ASTNode
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

internal abstract class SqlStmtListMixin(node: ASTNode) : SqliteCompositeElementImpl(node) {
  private val psiManager: PsiManager
    get() = PsiManager.getInstance(project)

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    val fileType = (parent as PsiFile).fileType
    val result = ArrayList<QueryResult>()
    ProjectRootManager.getInstance(project).fileIndex.iterateContent { file ->
      if (file.fileType != fileType) return@iterateContent true
      psiManager.findFile(file)?.run {
        findChildrenByClass(SqliteSqlStmt::class.java).forEach { sqlStmt ->
          sqlStmt.createTableStmt?.let { createTable ->
            createTable.compoundSelectStmt?.let {
              result.add(QueryResult(createTable.tableName, it.queryExposed().flatMap { it.columns }))
            }
            if (createTable.columnDefList.isNotEmpty()) {
              result.add(QueryResult(createTable.tableName, createTable.columnDefList.map { it.columnName }))
            }
          }
          sqlStmt.createViewStmt?.let { createView ->
            result.add(QueryResult(createView.viewName, createView.compoundSelectStmt.queryExposed().flatMap { it.columns }))
          }
        }
      }
      return@iterateContent true
    }
    return result
  }
}