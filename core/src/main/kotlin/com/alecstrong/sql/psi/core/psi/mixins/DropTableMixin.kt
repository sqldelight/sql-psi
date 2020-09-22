package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlDropTableStmt
import com.alecstrong.sql.psi.core.psi.TableElement
import com.intellij.lang.ASTNode

internal abstract class DropTableMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlDropTableStmt,
    SchemaContributor {
  override fun modifySchema(schema: Schema) {
    tableName?.let { tableName ->
      schema.forType<TableElement>().remove(tableName.name)
    }
  }
}
