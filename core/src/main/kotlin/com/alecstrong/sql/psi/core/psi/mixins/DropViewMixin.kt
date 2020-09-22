package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlDropViewStmt
import com.alecstrong.sql.psi.core.psi.TableElement
import com.intellij.lang.ASTNode

internal abstract class DropViewMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlDropViewStmt,
    SchemaContributor {
  override fun modifySchema(schema: Schema) {
    viewName?.let { viewName ->
      schema.forType<TableElement>().remove(viewName.text)
      schema.forType<SqlCreateViewStmt>().remove(viewName.text)
    }
  }
}
