package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlCreateTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlDropTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.impl.SqlDropTriggerStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

internal abstract class DropTriggerMixin(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?,
) :
  SqlSchemaContributorImpl<SqlCreateTriggerStmt, CreateTriggerElementType>(stub, nodeType, node),
  SqlDropTriggerStmt {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(stub: SchemaContributorStub, nodeType: IElementType) : this(stub, nodeType, null)

  override fun name() = triggerName?.text ?: ""

  override fun modifySchema(schema: Schema) {
    triggerName?.let { triggerName ->
      schema.forType<SqlCreateTriggerStmt>().remove(triggerName.text)
    }
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    triggerName?.let { triggerName ->
      if (
        node.findChildByType(SqlTypes.EXISTS) == null &&
          containingFile.schema<SqlCreateTriggerStmt>(this).none {
            it != this && it.triggerName.textMatches(triggerName)
          }
      ) {
        annotationHolder.createErrorAnnotation(
          triggerName,
          "No trigger found with name ${triggerName.text}",
        )
      }
    }

    super.annotate(annotationHolder)
  }
}

internal class DropTriggerElementType(name: String) :
  SqlSchemaContributorElementType<SqlCreateTriggerStmt>(name, SqlCreateTriggerStmt::class.java) {
  override fun nameType() = SqlTypes.TRIGGER_NAME

  override fun createPsi(stub: SchemaContributorStub) = SqlDropTriggerStmtImpl(stub, this)
}
