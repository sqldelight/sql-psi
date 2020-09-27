package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlCreateTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlDropTriggerStmt
import com.intellij.lang.ASTNode

internal abstract class DropTriggerMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlDropTriggerStmt,
    SchemaContributor {
  override fun modifySchema(schema: Schema) {
    triggerName?.let { triggerName ->
      schema.forType<SqlCreateTriggerStmt>().remove(triggerName.text)
    }
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    triggerName?.let { triggerName ->
      if (containingFile.schema<SqlCreateTriggerStmt>(this).none { it != this && it.triggerName.text == triggerName.text }) {
        annotationHolder.createErrorAnnotation(triggerName, "No trigger found with name ${triggerName.text}")
      }
    }

    super.annotate(annotationHolder)
  }
}
