package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCommonTableExpression
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.lang.ASTNode

internal abstract class CommonTableExpressionMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCommonTableExpression {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    val query = QueryResult(tableName, compoundSelectStmt.queryExposed().flatMap { it.columns })
    if (columnAliasList.isNotEmpty() && columnAliasList.size != query.columns.size) {
      annotationHolder.createErrorAnnotation(this, "Incorrect number of columns")
    }
  }

  override fun queryExposed(): List<QueryResult> {
    val query = QueryResult(tableName, compoundSelectStmt.queryExposed().flatMap { it.columns })
    if (columnAliasList.isNotEmpty()) return listOf(QueryResult(tableName, columnAliasList))
    return listOf(query)
  }
}