package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.IndexElement
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlDropIndexStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.impl.SqlDropIndexStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

internal abstract class DropIndexMixin
private constructor(stub: SchemaContributorStub?, nodeType: IElementType?, node: ASTNode?) :
  SqlSchemaContributorImpl<IndexElement, DropIndexElementType>(stub, nodeType, node),
  SqlDropIndexStmt {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(stub: SchemaContributorStub, nodeType: IElementType) : this(stub, nodeType, null)

  override fun name(): String {
    stub?.let {
      return it.name()
    }
    return indexName?.text ?: ""
  }

  override fun modifySchema(schema: Schema) {
    schema.forType<IndexElement>().remove(name())
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    indexName?.let { indexName ->
      if (
        node.findChildByType(SqlTypes.EXISTS) == null &&
          containingFile.schema<IndexElement>(this).none {
            it != this && it.indexElementName() == indexName.text
          }
      ) {
        annotationHolder.createErrorAnnotation(
          indexName,
          "No index found with name ${indexName.text}",
        )
      }
    }

    super.annotate(annotationHolder)
  }
}

internal class DropIndexElementType(name: String) :
  SqlSchemaContributorElementType<IndexElement>(name, IndexElement::class.java) {
  override fun nameType() = SqlTypes.TABLE_NAME

  override fun createPsi(stub: SchemaContributorStub) = SqlDropIndexStmtImpl(stub, this)
}
