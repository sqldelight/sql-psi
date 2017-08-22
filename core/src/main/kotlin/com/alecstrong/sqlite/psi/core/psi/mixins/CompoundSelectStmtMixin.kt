package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCommonTableExpression
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCompoundSelectStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteOrderingTerm
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteSelectStmt
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract internal class CompoundSelectStmtMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCompoundSelectStmt {
  override fun queryExposed() = selectStmtList.first().queryExposed()

  override fun tablesAvailable(child: PsiElement): List<QueryResult> {
    return super.tablesAvailable(child) + commonTableExpressionList.flatMap { it.queryExposed() }
  }

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child is SqliteOrderingTerm || child is SqliteExpr) {
      return selectStmtList.first().queryExposed()
    } else if (child is SqliteCommonTableExpression || child is SqliteSelectStmt) {
      return super.queryAvailable(child)
    }
    throw IllegalStateException("Unexpected child element asking for query: $child")
  }
}