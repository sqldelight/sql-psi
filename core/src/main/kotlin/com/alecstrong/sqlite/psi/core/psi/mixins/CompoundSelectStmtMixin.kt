package com.alecstrong.sqlite.psi.core.psi.mixins

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
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    val tablesAvailable = super.queryAvailable(child)
    if (child is SqliteSelectStmt) {
      return tablesAvailable + commonTableExpressionList.flatMap { it.queryExposed() }
    } else if (child is SqliteOrderingTerm || child is SqliteExpr) {
      return selectStmtList.first().queryExposed()
    }
    throw IllegalStateException("Unexpected child element asking for query: $child")
  }
}