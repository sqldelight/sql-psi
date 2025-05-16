package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.ModifiableFileLazy
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlJoinClause
import com.alecstrong.sql.psi.core.psi.SqlJoinConstraint
import com.alecstrong.sql.psi.core.psi.SqlJoinOperator
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet

internal abstract class JoinClauseMixin(
  node: ASTNode,
) : SqlCompositeElementImpl(node),
  SqlJoinClause {
  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child is SqlJoinConstraint) {
      val queryAvailable = tableOrSubqueryList[0].queryExposed()
        .map { it.copy(adjacent = true) }
        .plus(super.queryAvailable(child).map { it.copy(adjacent = false) })
        .toMutableList()
      tableOrSubqueryList.drop(1).zip(joinConstraintList)
        .forEach { (subquery, constraint) ->
          if (child == constraint) {
            if (child.node.findChildByType(
                SqlTypes.USING,
              ) != null
            ) {
              return listOf(
                QueryResult(
                  null,
                  queryAvailable.flatMap { it.columns }
                    .filter { (column, _) ->
                      column is PsiNamedElement && column.name!! in subquery.queryExposed()
                        .flatMap { it.columns }
                        .mapNotNull { (it.element as? PsiNamedElement)?.name }
                    }
                    .distinctBy { (it.element as? PsiNamedElement)?.name ?: it },
                ),
              )
            }
            return queryAvailable + subquery.queryExposed()
          }
          queryAvailable += subquery.queryExposed()
        }
      return queryExposed()
    }
    return super.queryAvailable(child)
  }

  private val queryExposed = ModifiableFileLazy {

    var queryAvailable: Collection<QueryResult> = tableOrSubqueryList.first().queryExposed()

    for ((index, subquery) in tableOrSubqueryList.zipWithNext().withIndex()) {

      val constraint: SqlJoinConstraint? = if (index in joinConstraintList.indices) joinConstraintList[index] else null
      val operator: SqlJoinOperator = joinOperatorList[index]

      val query = subquery.second.queryExposed()

      if (query.isEmpty()) continue

      var columns = query.flatMap { it.columns }
      var synthesizedColumns = query.flatMap { it.synthesizedColumns }

      if (rightJoinOperator(joinOperatorList[index])) {
        val rightQuery = subquery.first.queryExposed()
        var rightColumns = rightQuery.flatMap { it.columns }
        var rightSynthesizedColumns = rightQuery.flatMap { it.synthesizedColumns }

        rightColumns = rightColumns.map { it.copy(nullable = true) }
        rightSynthesizedColumns = rightSynthesizedColumns.map { it.copy(nullable = true) }

        queryAvailable -= rightQuery
        queryAvailable += QueryResult(
          table = rightQuery.first().table,
          columns = rightColumns,
          synthesizedColumns = rightSynthesizedColumns,
          joinConstraint = joinConstraintList[index],
        )
      }

      if (leftJoinOperator(operator)) {
        columns = columns.map { it.copy(nullable = true) }
        synthesizedColumns = synthesizedColumns.map { it.copy(nullable = true) }
      }

      if (constraint != null && usingConstraint(constraint)) {
        val columnNames = constraint.columnNameList.map { it.name }
        columns = columns.map {
          it.copy(hiddenByUsing = it.element is PsiNamedElement && it.element.name in columnNames)
        }
      }

      queryAvailable += QueryResult(
        table = query.first().table,
        columns = columns,
        synthesizedColumns = synthesizedColumns,
        joinConstraint = constraint,
      )
    }

    return@ModifiableFileLazy queryAvailable
  }

  private fun leftJoinOperator(operator: SqlJoinOperator): Boolean {
    return operator.node.findChildByType(
      TokenSet.create(
        SqlTypes.LEFT_JOIN_OPERATOR,
        SqlTypes.FULL_JOIN_OPERATOR,
      ),
    ) != null
  }

  private fun rightJoinOperator(operator: SqlJoinOperator): Boolean {
    return operator.node.findChildByType(
      TokenSet.create(
        SqlTypes.RIGHT_JOIN_OPERATOR,
        SqlTypes.FULL_JOIN_OPERATOR,
      ),
    ) != null
  }

  private fun usingConstraint(constraint: SqlJoinConstraint): Boolean {
    return constraint.node?.findChildByType(
      SqlTypes.USING,
    ) != null
  }
  override fun queryExposed() = queryExposed.forFile(containingFile)
}
