package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.SqlBindExpr
import com.alecstrong.sql.psi.core.psi.SqlColumnExpr
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlGroupBy
import com.alecstrong.sql.psi.core.psi.SqlSelectStmt
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class GroupByMixing(
  node: ASTNode,
) : SqlCompositeElementImpl(node),
  SqlGroupBy {
  override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
    return if (child is SqlColumnExpr) {
      (parent as SqlSelectStmt).queryAvailable(child)
    } else {
      super.queryAvailable(child)
    }
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)

    for (expr in exprList) {
      if (expr is SqlBindExpr) {
        annotationHolder.createErrorAnnotation(
          expr,
          "Cannot bind the name of a column in a GROUP BY clause",
        )
      }
    }
  }
}
