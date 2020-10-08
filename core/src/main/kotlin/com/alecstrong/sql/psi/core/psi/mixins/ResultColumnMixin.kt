package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.ModifiableFileLazy
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.FromQuery
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlBindExpr
import com.alecstrong.sql.psi.core.psi.SqlColumnExpr
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlResultColumn
import com.intellij.lang.ASTNode

internal abstract class ResultColumnMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlResultColumn {
  private val queryExposed = ModifiableFileLazy lazy@{
    val fromQuery = (parent as? FromQuery)?.fromQuery() ?: return@lazy emptyList<QueryResult>()
    tableName?.let { tableNameElement ->
      // table_name '.' '*'
      return@lazy fromQuery.filter { it.table?.name == tableNameElement.name }
    }
    expr?.let {
      var column: QueryElement.QueryColumn
      if (it is SqlColumnExpr) {
        val reference = (it.columnName as ColumnNameMixin).reference
        column = reference.resolveToQuery()
            ?: QueryElement.QueryColumn(it.columnName.reference?.resolve() ?: it)
      } else {
        column = QueryElement.QueryColumn(it)
      }

      // expr [ '.' column_alias ]
      columnAlias?.let { alias ->
        column = column.copy(element = alias)
      }

      return@lazy listOf(QueryResult(columns = listOf(column)))
    }

    // *
    val queryAvailable = fromQuery
    if (queryAvailable.size <= 1) {
      return@lazy queryAvailable
    }

    return@lazy queryAvailable.fold(emptyList<QueryResult>()) { left, right ->
      left + right.copy(table = null, columns = right.columns.filter { !it.hiddenByUsing })
    }
  }

  override fun queryExposed() = queryExposed.forFile(containingFile)

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)

    val bindExpr = expr as? SqlBindExpr
    if (bindExpr != null) {
      annotationHolder.createErrorAnnotation(bindExpr, "Cannot bind the name of a column in a SELECT result-column")
    }
  }
}
