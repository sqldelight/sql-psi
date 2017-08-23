package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteJoinClause
import com.alecstrong.sqlite.psi.core.psi.SqliteJoinConstraint
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

abstract internal class JoinClauseMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteJoinClause {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child is SqliteJoinConstraint) {
      if (child.node.findChildByType(SqliteTypes.USING) != null) {
        // Only include columns that are in both tables
        return listOf(QueryResult(null, tableOrSubqueryList[0].queryExposed()
            .flatMap { it.columns }
            .filterIsInstance<PsiNamedElement>()
            .filter {
              it.name!! in tableOrSubqueryList[1].queryExposed()
                  .flatMap { it.columns }
                  .filterIsInstance<PsiNamedElement>()
                  .mapNotNull { it.name }
            }))
      }
      return queryExposed()
    }
    return super.queryAvailable(child)
  }

  override fun queryExposed(): List<QueryResult> {
    return tableOrSubqueryList.flatMap { it.queryExposed() }
  }
}