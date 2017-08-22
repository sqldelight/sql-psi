package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteSelectStmt
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class SelectStmtMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteSelectStmt {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child in resultColumnList) return fromQuery()
    if (child in exprList) return fromQuery() + super.queryAvailable(this)
    if (child in tableOrSubqueryList) return super.queryAvailable(child)
    if (child == joinClause) return super.queryAvailable(child)
    return super.queryAvailable(child)
  }

  override fun queryExposed(): List<QueryResult> {
    if (valuesExpressionList.isNotEmpty()) {
      return listOf(QueryResult(null, valuesExpressionList.map { it.expr }))
    }
    return resultColumnList.flatMap { it.queryExposed() }
  }

  private fun fromQuery(): List<QueryResult> {
    joinClause?.let {
      return it.queryExposed()
    }
    if (tableOrSubqueryList.isNotEmpty()) {
      return tableOrSubqueryList.flatMap { it.queryExposed() }
    }
    return emptyList()
  }
}