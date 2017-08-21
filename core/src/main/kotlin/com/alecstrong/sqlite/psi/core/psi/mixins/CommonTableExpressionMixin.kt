package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCommonTableExpression
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.lang.ASTNode

internal abstract class CommonTableExpressionMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCommonTableExpression {
  override fun queryExposed(): List<QueryResult> {
    val query = QueryResult(tableName, compoundSelectStmt.queryExposed().flatMap { it.columns })
    if (columnAliasList.isNotEmpty()) {
      if (columnAliasList.size != query.columns.size) {
        // TODO (AlecStrong) : Annotate the column list with an error of incorrect size
        return listOf(QueryResult(tableName, columnAliasList))
      }
    }
    return listOf(query)
  }
}