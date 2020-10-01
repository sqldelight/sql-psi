package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlCreateIndexStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.impl.SqlCreateIndexStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

internal abstract class CreateIndexMixin(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?
) : SqlSchemaContributorImpl<SqlCreateIndexStmt, CreateIndexElementType>(stub, nodeType, node),
    SqlCreateIndexStmt {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(
    stub: SchemaContributorStub,
    nodeType: IElementType
  ) : this(stub, nodeType, null)

  override fun name(): String {
    stub?.let { return it.name() }
    return indexName.text
  }

  override fun modifySchema(schema: Schema) {
    val indexes = schema.forType<SqlCreateIndexStmt>()
    indexes.putValue(name(), this)
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child in indexedColumnList || child == expr) {
      return listOfNotNull(
          tablesAvailable(child).firstOrNull { it.tableName.name == tableName?.name }?.query
      )
    }
    return super.queryAvailable(child)
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    if (containingFile.schema<SqlCreateIndexStmt>(this).any { it != this && it.indexName.text == indexName.text }) {
      annotationHolder.createErrorAnnotation(indexName, "Duplicate index name ${indexName.text}")
    }
    super.annotate(annotationHolder)
  }
}

internal class CreateIndexElementType(
  name: String
) : SqlSchemaContributorElementType<SqlCreateIndexStmt>(name, SqlCreateIndexStmt::class.java) {
  override fun nameType() = SqlTypes.TABLE_NAME
  override fun createPsi(stub: SchemaContributorStub) = SqlCreateIndexStmtImpl(stub, this)
}
