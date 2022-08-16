package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.FromQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlSetStmt
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class SetStmtMixin(
  node: ASTNode,
) : SqlCompositeElementImpl(node),
  SqlSetStmt,
  FromQuery {

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    val selectStmt = selectStmt ?: return emptyList()
    return if (child in selectStmt.children) {
      selectStmt.queryAvailable(child)
    } else emptyList()
  }

  override fun queryExposed(): Collection<QueryResult> {
    val selectStmt = selectStmt ?: return emptyList()
    return selectStmt.queryExposed()
  }

  override fun fromQuery(): Collection<QueryResult> {
    selectStmt?.joinClause?.let {
      return it.queryExposed()
    }
    return emptyList()
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)
    selectStmt?.annotate(annotationHolder)
  }
}
