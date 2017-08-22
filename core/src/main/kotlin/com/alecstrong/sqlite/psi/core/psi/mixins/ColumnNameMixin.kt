package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.AnnotationException
import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnReference
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement

internal abstract class ColumnNameMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    PsiNamedElement {
  private var hardcodedName: String? = null

  override fun getReference() = SqliteColumnReference(this)
  override fun getName(): String = hardcodedName ?: text
  override fun setName(name: String) = apply { hardcodedName = name }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    try {
      val source = reference.unsafeResolve()
      if (source == null) {
        annotationHolder.createErrorAnnotation(this, "No column found with name $name")
      }
    } catch (e: AnnotationException) {
      annotationHolder.createErrorAnnotation(this, e.message)
    }
  }
}
