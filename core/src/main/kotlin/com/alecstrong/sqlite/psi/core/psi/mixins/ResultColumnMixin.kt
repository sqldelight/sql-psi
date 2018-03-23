package com.alecstrong.sqlite.psi.core.psi.mixins

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
      if (it is SqliteColumnExpr) {
        return listOf(QueryResult(null, listOf(it.columnName.reference?.resolve() ?: it)))
      }
      return listOf(QueryResult(null, listOf(it)))
    }

    // *
    val queryAvailable = queryAvailable(this)
    if (queryAvailable.size <= 1) return queryAvailable

    val leftmostQuery = QueryResult(table = null, columns = queryAvailable.first().columns)
    return queryAvailable.drop(1).fold(listOf(leftmostQuery)) { left, right ->
      if (right.joinConstraint?.node?.findChildByType(SqliteTypes.USING) != null) {
        val columnNames = right.joinConstraint.columnNameList.map { it.name }
        return@fold left + right.copy(
            table = null,
            columns = right.columns
                .filterIsInstance<PsiNamedElement>()
                .filter { it.name !in columnNames }
        )
      } else {
        return@fold left + right.copy(table = null)
      }
    }
  }
}
