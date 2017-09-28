package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteFileBase
import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class SqlStmtListMixin(node: ASTNode) : SqliteCompositeElementImpl(node) {
  override fun tablesAvailable(child: PsiElement): List<LazyQuery> {
    return (parent as SqliteFileBase).tablesAvailable(child)
  }

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    return emptyList()
  }
}