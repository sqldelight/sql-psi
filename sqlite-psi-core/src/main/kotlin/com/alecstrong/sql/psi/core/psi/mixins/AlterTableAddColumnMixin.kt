package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.AlterTableApplier
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.SqlAlterTableAddColumn
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.intellij.lang.ASTNode

internal abstract class AlterTableAddColumnMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
  SqlAlterTableAddColumn,
  AlterTableApplier {
  override fun applyTo(lazyQuery: LazyQuery): LazyQuery {
    return LazyQuery(
      tableName = lazyQuery.tableName,
      query = {
        lazyQuery.query.copy(
          columns = lazyQuery.query.columns + QueryElement.QueryColumn(columnDef.columnName)
        )
      }
    )
  }
}
