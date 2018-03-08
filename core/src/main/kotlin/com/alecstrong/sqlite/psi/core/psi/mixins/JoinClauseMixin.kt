package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteJoinClause
import com.alecstrong.sqlite.psi.core.psi.SqliteJoinConstraint
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
      var queryAvailable = tableOrSubqueryList[0].queryExposed()
      tableOrSubqueryList.drop(1).zip(joinConstraintList)
          .forEach { (subquery, constraint) ->
            if (child == constraint) {
              if (child.node.findChildByType(SqliteTypes.USING) != null) {
                return listOf(QueryResult(null, queryAvailable.flatMap { it.columns }
                    .filterIsInstance<PsiNamedElement>()
                    .filter {
                      it.name!! in subquery.queryExposed()
                          .flatMap { it.columns }
                          .filterIsInstance<PsiNamedElement>()
                          .mapNotNull { it.name }
                    }))
              }
              return queryAvailable + subquery.queryExposed()
            }
            queryAvailable += subquery.queryExposed()
          }
      return queryExposed()
    }
    return super.queryAvailable(child)
  }

  override fun queryExposed(): List<QueryResult> {
    var queryAvailable = tableOrSubqueryList[0].queryExposed()
    tableOrSubqueryList.drop(1)
        .zip(joinConstraintList)
        .zip(joinOperatorList) { (subquery, constraint), operator ->
          val subqueryExposed = subquery.queryExposed().let { query ->
            when {
              query.isEmpty() -> return@zip
              query.size == 1 -> query.single().copy(joinOperator = operator)
              else -> QueryResult(
                  table = query.first().table,
                  columns = query.flatMap { it.columns },
                  joinOperator = operator
              )
            }
          }
          if (constraint.node.findChildByType(SqliteTypes.USING) != null) {
            val columnNames = constraint.columnNameList.map { it.name }
            queryAvailable += subqueryExposed.copy(
                columns = subqueryExposed.columns
                    .filterIsInstance<PsiNamedElement>()
                    .filter { it.name !in columnNames }
            )
          } else {
            queryAvailable += subqueryExposed
          }
        }
    return queryAvailable
  }
}