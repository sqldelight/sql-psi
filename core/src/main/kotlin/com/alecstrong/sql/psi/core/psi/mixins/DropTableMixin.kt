package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlDropTableStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.impl.SqlDropTableStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

internal abstract class DropTableMixin private constructor(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?
) : SqlSchemaContributorImpl<TableElement, DropTableElementType>(stub, nodeType, node),
  SqlDropTableStmt {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(
    stub: SchemaContributorStub,
    nodeType: IElementType
  ) : this(stub, nodeType, null)

  override fun name(): String {
    stub?.let { return it.name() }
    return tableName?.name ?: ""
  }

  override fun modifySchema(schema: Schema) {
    schema.forType<TableElement>().remove(name())
  }
}

internal class DropTableElementType(
  name: String
) : SqlSchemaContributorElementType<TableElement>(name, TableElement::class.java) {
  override fun nameType() = SqlTypes.TABLE_NAME
  override fun createPsi(stub: SchemaContributorStub) = SqlDropTableStmtImpl(stub, this)
}
