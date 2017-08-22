package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

internal abstract class CreateTriggerMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateTriggerStmt {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    if (PsiTreeUtil.getParentOfType(this, SqlStmtListMixin::class.java)!!.triggers()
          .any { it != this && it.triggerName.text == triggerName.text }) {
      annotationHolder.createErrorAnnotation(triggerName,
          "Duplicate trigger name ${triggerName.text}")
    }
  }
}