package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteResultColumn
import com.intellij.lang.ASTNode

internal abstract class ResultColumnMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteResultColumn {
  override fun queryExposed(): List<QueryResult> {
    tableName?.let { tableNameElement ->
      // table_name '.' '*'
      return queryAvailable(this).filter { it.table?.name == tableNameElement.name }
    }
    expr?.let {
      // expr [ '.' column_alias ]
      columnAlias?.let { alias ->
        return listOf(QueryResult(null, listOf(alias)))
      }
      return listOf(QueryResult(null, listOf(it)))
    }

    // *
    return queryAvailable(this)
  }
}