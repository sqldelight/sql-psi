package com.alecstrong.sql.psi.core.mysql.psi.mixins

import com.alecstrong.sql.psi.core.mysql.psi.MySqlAlterTableDropColumn
import com.alecstrong.sql.psi.core.psi.AlterTableApplier
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.intellij.lang.ASTNode

internal abstract class AlterTableDropColumnMixin(
    node: ASTNode
) : SqlCompositeElementImpl(node),
    MySqlAlterTableDropColumn,
    AlterTableApplier {
  override fun applyTo(lazyQuery: LazyQuery): LazyQuery {
    return LazyQuery(
        tableName = lazyQuery.tableName,
        query = {
          val columns = lazyQuery.query.columns.filter { it.element.text != node.lastChildNode.text }
          lazyQuery.query.copy(columns = columns)
        }
    )
  }
}
