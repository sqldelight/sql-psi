package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteTableOrSubquery
import com.intellij.lang.ASTNode

internal abstract class TableOrSubqueryMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteTableOrSubquery {
  override fun queryExposed(): List<QueryResult> {
    tableName?.let { tableNameElement ->
      val result = tableAvailable(tableNameElement, tableNameElement.name)
      if (result.isEmpty()) {
        return emptyList()
      }
      tableAlias?.let { alias ->
        return listOf(QueryResult(alias, result.flatMap { it.columns }))
      }
      return result
    }
    compoundSelectStmt?.let {
      val result = it.queryExposed()
      tableAlias?.let { alias ->
        return result.map { it.copy(table = alias) }
      }
      return result
    }
    joinClause?.let { return it.queryExposed() }
    return tableOrSubqueryList.flatMap { it.queryExposed() }
  }
}