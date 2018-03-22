package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.SqliteFileBase
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal open class SqliteCompositeElementImpl(
    node: ASTNode
) : ASTWrapperPsiElement(node),
    SqliteCompositeElement {
  private val analytics = mutableMapOf<String, Analytics>()

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    return (parent as SqliteCompositeElement).queryAvailable(this)
  }

  override fun tablesAvailable(child: PsiElement): List<LazyQuery> {
    return (parent as SqliteCompositeElement).tablesAvailable(this)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) = Unit

  override fun toString(): String {
    if (parent !is SqliteCompositeElement) return super.toString()
    return "${super.toString()}: ${(parent as SqliteCompositeElement).queryAvailable(this)}"
  }

  inline fun <T> analyze(key: String, block: () -> T): T {
    val start = System.currentTimeMillis()
    try {
      return block()
    } finally {
      val duration = System.currentTimeMillis() - start
      analytics.put(key, analytics.getOrDefault(key, Analytics()) + duration)
    }
  }

  protected fun tableAvailable(child: PsiElement, name: String): List<QueryResult> {
    return tablesAvailable(child).filter { it.tableName.name == name }.map { it.query }
  }

  override fun analytics() = analytics

  override fun getContainingFile() = super.getContainingFile() as SqliteFileBase
}