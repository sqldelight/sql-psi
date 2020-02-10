package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlFileBase
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class SqlStmtListMixin(node: ASTNode) : SqlCompositeElementImpl(node) {
  override fun tablesAvailable(child: PsiElement): Collection<LazyQuery> {
    return (parent as SqlFileBase).tablesAvailable(child)
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    return emptyList()
  }
}