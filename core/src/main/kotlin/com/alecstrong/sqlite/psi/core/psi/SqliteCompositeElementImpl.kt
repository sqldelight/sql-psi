package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal open class SqliteCompositeElementImpl(
    node: ASTNode
) : ASTWrapperPsiElement(node),
    SqliteCompositeElement {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    return (parent as SqliteCompositeElement).queryAvailable(this)
  }
}