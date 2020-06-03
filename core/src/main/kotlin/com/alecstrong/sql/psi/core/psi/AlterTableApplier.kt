package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.AnnotationException

internal interface AlterTableApplier {
  fun applyTo(lazyQuery: LazyQuery): LazyQuery
}

internal fun LazyQuery.withAlterStatement(alter: SqlAlterTableStmt): LazyQuery {
  return alter.alterTableRulesList.fold(this, { lazyQuery, alterTableRules ->
    // Add column.
    alterTableRules.alterTableAddColumn?.let { addColumn ->
      return@fold LazyQuery(
          tableName = tableName,
          query = {
            lazyQuery.query.copy(columns = lazyQuery.query.columns + QueryElement.QueryColumn(addColumn.columnDef.columnName))
          }
      )
    }

    // Rename table.
    alterTableRules.alterTableRenameTable?.let { renameTable ->
      return@fold LazyQuery(
          tableName = renameTable.newTableName,
          query = {
            lazyQuery.query.copy(table = renameTable.newTableName)
          }
      )
    }

    (alterTableRules.firstChild as? AlterTableApplier)?.let {
      return it.applyTo(lazyQuery)
    }

    throw AnnotationException("Unhandled alter rule", alterTableRules)
  })
}
