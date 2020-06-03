package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.AnnotationException

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
    alterTableRules.alterTableRenameTable?.let { renametable ->
      return@fold LazyQuery(
          tableName = renametable.newTableName,
          query = {
            lazyQuery.query.copy(table = renametable.newTableName)
          }
      )
    }

    throw AnnotationException("Unhandled alter rule", alterTableRules)
  })
}
