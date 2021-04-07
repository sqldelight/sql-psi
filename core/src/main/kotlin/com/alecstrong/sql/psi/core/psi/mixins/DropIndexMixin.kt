package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlCreateIndexStmt
import com.alecstrong.sql.psi.core.psi.SqlDropIndexStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.impl.SqlDropIndexStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

internal abstract class DropIndexMixin private constructor(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?
) : SqlSchemaContributorImpl<SqlCreateIndexStmt, DropIndexElementType>(stub, nodeType, node),
  SqlDropIndexStmt {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(
    stub: SchemaContributorStub,
    nodeType: IElementType
  ) : this(stub, nodeType, null)

  override fun name(): String {
    stub?.let { return it.name() }
    return indexName?.text ?: ""
  }

  override fun modifySchema(schema: Schema) {
    schema.forType<SqlCreateIndexStmt>().remove(name())
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    indexName?.let { indexName ->
      if (node.findChildByType(SqlTypes.EXISTS) == null &&
        containingFile.schema<SqlCreateIndexStmt>(this)
          .none { it != this && it.indexName.textMatches(indexName) }
      ) {
        annotationHolder.createErrorAnnotation(
          indexName,
          "No index found with name ${indexName.text}"
        )
      }
    }

    super.annotate(annotationHolder)
  }
}

internal class DropIndexElementType(
  name: String
) : SqlSchemaContributorElementType<SqlCreateIndexStmt>(name, SqlCreateIndexStmt::class.java) {
  override fun nameType() = SqlTypes.TABLE_NAME
  override fun createPsi(stub: SchemaContributorStub) = SqlDropIndexStmtImpl(stub, this)
}
