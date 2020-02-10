package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlFileBase
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal open class SqlCompositeElementImpl(
    node: ASTNode
) : ASTWrapperPsiElement(node),
    SqlCompositeElement {
  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    return (parent as SqlCompositeElement).queryAvailable(this)
  }

  override fun tablesAvailable(child: PsiElement): Collection<LazyQuery> {
    return (parent as SqlCompositeElement).tablesAvailable(this)
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) = Unit

  override fun toString(): String {
    if (parent !is SqlCompositeElement) return super.toString()
    return "${super.toString()}: ${(parent as SqlCompositeElement).queryAvailable(this)}"
  }

  protected fun tableAvailable(child: PsiElement, name: String): Collection<QueryResult> {
    return tablesAvailable(child).filter { it.tableName.name == name }.map { it.query }
  }

  override fun getContainingFile() = super.getContainingFile() as SqlFileBase
}