package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteInExpr
import com.intellij.lang.ASTNode

internal abstract class InExprMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteInExpr {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    val query = compoundSelectStmt?.queryExposed()
        ?: tableName?.let { table -> tableAvailable(this, table.name) }
        ?: emptyList()
    if (query.flatMap { it.columns }.size > 1) {
      annotationHolder.createErrorAnnotation(this,
          "Only a single result allowed for a SELECT that is part of an expression")
    }
  }
}