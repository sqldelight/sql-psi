package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteExistsExpr
import com.intellij.lang.ASTNode

internal abstract class ExistsExprMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteExistsExpr {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    if (compoundSelectStmt.queryExposed().flatMap { it.columns }.size > 1) {
      annotationHolder.createErrorAnnotation(this, "Expression subquery must have exactly one value")
    }
  }
}