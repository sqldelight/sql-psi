package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.ModifiableFileLazy
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteTableOrSubquery
import com.intellij.lang.ASTNode

internal abstract class TableOrSubqueryMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteTableOrSubquery {
  private val queryExposed: List<QueryResult> by ModifiableFileLazy(containingFile) lazy@{
    tableName?.let { tableNameElement ->
      val result = tableAvailable(tableNameElement, tableNameElement.name)
      if (result.isEmpty()) {
        return@lazy emptyList<QueryResult>()
      }
      tableAlias?.let { alias ->
        return@lazy listOf(QueryResult(alias, result.flatMap { it.columns }))
      }
      return@lazy result
    }
    compoundSelectStmt?.let {
      val result = it.queryExposed()
      tableAlias?.let { alias ->
        return@lazy result.map { it.copy(table = alias) }
      }
      return@lazy result
    }
    joinClause?.let { return@lazy it.queryExposed() }
    return@lazy tableOrSubqueryList.flatMap { it.queryExposed() }
  }

  override fun queryExposed() = queryExposed
}