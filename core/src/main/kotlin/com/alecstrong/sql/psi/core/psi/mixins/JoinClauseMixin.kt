package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.ModifiableFileLazy
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlJoinClause
import com.alecstrong.sql.psi.core.psi.SqlJoinConstraint
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

internal abstract class JoinClauseMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlJoinClause {
  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child is SqlJoinConstraint) {
      val queryAvailable = tableOrSubqueryList[0].queryExposed()
          .map { it.copy(adjacent = true) }
          .plus(super.queryAvailable(child))
          .toMutableList()
      tableOrSubqueryList.drop(1).zip(joinConstraintList)
          .forEach { (subquery, constraint) ->
            if (child == constraint) {
              if (child.node.findChildByType(
                      SqlTypes.USING) != null) {
                return listOf(QueryResult(null, queryAvailable.flatMap { it.columns }
                    .filter { (column, _) ->
                      column is PsiNamedElement && column.name!! in subquery.queryExposed()
                          .flatMap { it.columns }
                          .mapNotNull { (it.element as? PsiNamedElement)?.name }
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

  private val queryExposed = ModifiableFileLazy {
    var queryAvailable = tableOrSubqueryList[0].queryExposed()
    tableOrSubqueryList.drop(1)
        .zip(joinConstraintList)
        .zip(joinOperatorList) zip2@{ (subquery, constraint), operator ->
          queryAvailable += subquery.queryExposed().let { query ->
            when {
              query.isEmpty() -> return@zip2
              else -> {
                var columns = query.flatMap { it.columns }
                var synthesizedColumns = query.flatMap { it.synthesizedColumns }

                if (operator.node.findChildByType(
                        SqlTypes.LEFT) != null) {
                  columns = columns.map { it.copy(nullable = true) }
                  synthesizedColumns = synthesizedColumns.map { it.copy(nullable = true) }
                }
                if (constraint.node?.findChildByType(
                        SqlTypes.USING) != null) {
                  val columnNames = constraint.columnNameList.map { it.name }
                  columns = columns.map {
                    it.copy(hiddenByUsing = it.element is PsiNamedElement && it.element.name in columnNames)
                  }
                }
                QueryResult(
                        table = query.first().table,
                        columns = columns,
                        synthesizedColumns = synthesizedColumns,
                        joinConstraint = constraint)
              }
            }
          }
        }
    return@ModifiableFileLazy queryAvailable
  }

  override fun queryExposed() = queryExposed.forFile(containingFile)
}
