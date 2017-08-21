package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteJoinClause
import com.alecstrong.sqlite.psi.core.psi.SqliteJoinConstraint
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract internal class JoinClauseMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteJoinClause {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child is SqliteJoinConstraint) return tableOrSubqueryList.flatMap { it.queryExposed() }
    return super.queryAvailable(child)
  }

  override fun queryExposed(): List<QueryResult> {
    return tableOrSubqueryList.flatMap { it.queryExposed() }
  }
}