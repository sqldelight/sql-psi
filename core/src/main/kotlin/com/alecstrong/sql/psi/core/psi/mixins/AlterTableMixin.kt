package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.AnnotationException
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.NamedElement
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlAlterTableRules
import com.alecstrong.sql.psi.core.psi.SqlAlterTableStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.impl.SqlAlterTableStmtImpl
import com.alecstrong.sql.psi.core.psi.withAlterStatement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

internal abstract class AlterTableMixin private constructor(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?
) : SqlSchemaContributorImpl<TableElement, AlterTableElementType>(stub, nodeType, node),
    SqlAlterTableStmt,
    TableElement {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(
    stub: SchemaContributorStub,
    nodeType: IElementType
  ) : this(stub, nodeType, null)

  fun tableName(): NamedElement = alterTableRulesList
      .mapNotNull { it.alterTableRenameTable?.newTableName }
      .lastOrNull() ?: tableName!!

  override fun name(): String {
    stub?.let { return it.name() }
    return tableName!!.name
  }

  override fun modifySchema(schema: Schema) {
    schema.forType<TableElement>().remove(name())
    schema.forType<TableElement>().putValue(tableName().name, this)
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
    if (child in alterTableRulesList) {
      check(child is SqlAlterTableRules)
      return tablesAvailable(this)
          .filter { it.tableName.text == tableName!!.text }
          .map { it.withAlterStatement(this, until = child).query }
    }
    return super.queryAvailable(child)
  }

  override fun tableExposed(): LazyQuery {
    return LazyQuery(
        tableName = tableName(),
        query = result@{
          val tableName = (tableName?.reference?.resolve()?.parent as? TableElement)
              ?: return@result QueryElement.QueryResult(columns = emptyList())
          val lazyQuery = tableName.tableExposed()
          try {
            lazyQuery.withAlterStatement(this)
          } catch (e: AnnotationException) {
            lazyQuery
          }.query
        }
    )
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    if (containingFile.order == null) {
      annotationHolder.createErrorAnnotation(this, "Alter table statements are forbidden outside of migration files.")
      return
    }
    when (tableName?.reference?.resolve()?.parent) {
      is AlterTableMixin, is CreateTableMixin -> {}
      else -> {
        annotationHolder.createErrorAnnotation(
            tableName ?: this,
            "Attempting to alter something that is not a table."
        )
        return
      }
    }
    try {
      (tableName!!.reference!!.resolve()!!.parent as TableElement)
          .tableExposed()
          .withAlterStatement(this)
    } catch (e: AnnotationException) {
      annotationHolder.createErrorAnnotation(e.element ?: this, e.msg)
    }
    super.annotate(annotationHolder)
  }
}

internal class AlterTableElementType(
  name: String
) : SqlSchemaContributorElementType<TableElement>(name, TableElement::class.java) {
  override fun nameType() = SqlTypes.TABLE_NAME
  override fun createPsi(stub: SchemaContributorStub) = SqlAlterTableStmtImpl(stub, this)
}
