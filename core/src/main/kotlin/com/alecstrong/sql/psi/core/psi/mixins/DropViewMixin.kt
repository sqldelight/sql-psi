package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlDropViewStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.impl.SqlDropViewStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

internal abstract class DropViewMixin(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?,
) : SqlSchemaContributorImpl<TableElement, DropViewElementType>(stub, nodeType, node),
  SqlDropViewStmt {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(
    stub: SchemaContributorStub,
    nodeType: IElementType,
  ) : this(stub, nodeType, null)

  override fun name(): String {
    stub?.let { return it.name() }
    return viewName?.text ?: ""
  }

  override fun modifySchema(schema: Schema) {
    schema.forType<TableElement>().remove(name())
    schema.forType<SqlCreateViewStmt>().remove(name())
  }
}

internal class DropViewElementType(name: String) :
  SqlSchemaContributorElementType<TableElement>(name, TableElement::class.java) {
  override fun nameType() = SqlTypes.VIEW_NAME
  override fun createPsi(stub: SchemaContributorStub) = SqlDropViewStmtImpl(stub, this)
}
