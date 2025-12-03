package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.SqlBinaryExpr
import com.alecstrong.sql.psi.core.psi.SqlBindExpr
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.intellij.lang.ASTNode

internal abstract class BinaryComparisonExprMixin(node: ASTNode) :
  SqlCompositeElementImpl(node), SqlBinaryExpr {
  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)

    if (firstChild is SqlBindExpr && lastChild is SqlBindExpr) {
      annotationHolder.createErrorAnnotation(
        this,
        "Cannot bind both sides of a binary comparison expression",
      )
    }
  }
}
