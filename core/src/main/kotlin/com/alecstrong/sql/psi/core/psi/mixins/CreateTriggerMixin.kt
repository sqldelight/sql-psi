package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCreateTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlExpr
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.impl.SqlCreateTriggerStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

internal abstract class CreateTriggerMixin(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?
) : SqlSchemaContributorImpl<SqlCreateTriggerStmt, CreateTriggerElementType>(stub, nodeType, node),
  SqlCreateTriggerStmt {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(
    stub: SchemaContributorStub,
    nodeType: IElementType
  ) : this(stub, nodeType, null)

  override fun name(): String {
    stub?.let { return it.name() }
    return triggerName.text
  }

  override fun modifySchema(schema: Schema) {
    schema.put<SqlCreateTriggerStmt>(this)
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child is MutatorMixin || child is SqlExpr || child is CompoundSelectStmtMixin) {
      val table = tablesAvailable(this).firstOrNull {
        it.tableName.name == tableName?.name
      }?.query ?: return super.queryAvailable(child)

      if (hasElement(SqlTypes.INSERT)) {
        return listOf(
          QueryResult(
            SingleRow(tableName!!, "new"), table.columns,
            synthesizedColumns = table.synthesizedColumns
          )
        )
      }
      if (hasElement(SqlTypes.UPDATE)) {
        return listOf(
          QueryResult(
            SingleRow(tableName!!, "new"), table.columns,
            synthesizedColumns = table.synthesizedColumns
          ),
          QueryResult(
            SingleRow(tableName!!, "old"), table.columns,
            synthesizedColumns = table.synthesizedColumns
          )
        )
      }
      if (hasElement(SqlTypes.DELETE)) {
        return listOf(
          QueryResult(
            SingleRow(tableName!!, "old"), table.columns,
            synthesizedColumns = table.synthesizedColumns
          )
        )
      }
    }
    if (child is SqlColumnName) {
      return listOfNotNull(
        tablesAvailable(this).firstOrNull { it.tableName.name == tableName?.name }?.query
      )
    }
    return super.queryAvailable(child)
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    if (node.findChildByType(SqlTypes.EXISTS) == null &&
      containingFile.schema<SqlCreateTriggerStmt>(this)
        .any { it != this && it.triggerName.textMatches(triggerName) }
    ) {
      annotationHolder.createErrorAnnotation(
        triggerName,
        "Duplicate trigger name ${triggerName.text}"
      )
    }
  }

  private fun hasElement(elementType: IElementType): Boolean {
    val child = node.findChildByType(elementType) ?: return false
    return child.treeParent == node
  }
}

internal class CreateTriggerElementType(name: String) :
  SqlSchemaContributorElementType<SqlCreateTriggerStmt>(name, SqlCreateTriggerStmt::class.java) {
  override fun nameType() = SqlTypes.TRIGGER_NAME
  override fun createPsi(stub: SchemaContributorStub) = SqlCreateTriggerStmtImpl(stub, this)
}
