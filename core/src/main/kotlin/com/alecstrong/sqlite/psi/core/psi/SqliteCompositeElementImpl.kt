package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
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

  override fun tablesAvailable(child: PsiElement): List<QueryResult> {
    return (parent as SqliteCompositeElement).tablesAvailable(this)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) = Unit

  override fun toString(): String {
    if (parent !is SqliteCompositeElement) return super.toString()
    return "${super.toString()}: ${(parent as SqliteCompositeElement).queryAvailable(this)}"
  }
}