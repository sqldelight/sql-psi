package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.ModifiableFileLazy
import com.alecstrong.sqlite.psi.core.psi.QueryElement
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteResultColumn
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement

internal abstract class ResultColumnMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteResultColumn {
  private val queryExposed: Collection<QueryResult> by ModifiableFileLazy(containingFile) lazy@{
    tableName?.let { tableNameElement ->
      // table_name '.' '*'
      return@lazy queryAvailable(this).filter { it.table?.name == tableNameElement.name }
    }
    expr?.let {
      var column: QueryElement.QueryColumn
      if (it is SqliteColumnExpr) {
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
    val queryAvailable = queryAvailable(this)
    if (queryAvailable.size <= 1) {
      return@lazy queryAvailable
    }

    return@lazy queryAvailable.fold(emptyList<QueryResult>()) { left, right ->
      left + right.copy(table = null, columns = right.columns.filter { !it.hiddenByUsing })
    }
  }

  override fun queryExposed() = queryExposed
}
