package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.SqlBindExpr
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlOrderingTerm
import com.intellij.lang.ASTNode

internal abstract class OrderByMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
  SqlOrderingTerm {
  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)

    if (expr is SqlBindExpr) {
      annotationHolder.createErrorAnnotation(expr, "Cannot bind the name of a column in an ORDER BY clause")
    }
  }
}
