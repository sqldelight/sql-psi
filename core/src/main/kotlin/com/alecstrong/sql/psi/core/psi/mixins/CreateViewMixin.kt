package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.asColumns
import com.alecstrong.sql.psi.core.psi.impl.SqlCreateViewStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

internal abstract class CreateViewMixin(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?,
) : SqlSchemaContributorImpl<TableElement, CreateViewElementType>(stub, nodeType, node),
  SqlCreateViewStmt {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(
    stub: SchemaContributorStub,
    nodeType: IElementType,
  ) : this(stub, nodeType, null)

  override fun name(): String {
    stub?.let { return it.name() }
    return viewName.name
  }

  override fun modifySchema(schema: Schema) {
    schema.put<TableElement>(this)
    schema.put<SqlCreateViewStmt>(this)
  }

  override fun tableExposed() = LazyQuery(viewName) {
    val columns =
      if (columnAliasList.isEmpty()) {
        compoundSelectStmt?.queryExposed()?.flatMap { it.columns }
      } else {
        columnAliasList.asColumns()
      }

    QueryResult(viewName, columns ?: emptyList())
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)
    if (columnAliasList.isNotEmpty()) {
      if (columnAliasList.size != compoundSelectStmt?.queryExposed()?.map { it.columns }?.size) {
        annotationHolder.createErrorAnnotation(
          this,
          "number of aliases is different from the number of columns",
        )
      }
    }
  }
}

internal class CreateViewElementType(name: String) :
  SqlSchemaContributorElementType<TableElement>(name, TableElement::class.java) {
  override fun nameType() = SqlTypes.VIEW_NAME
  override fun createPsi(stub: SchemaContributorStub) = SqlCreateViewStmtImpl(stub, this)
}
