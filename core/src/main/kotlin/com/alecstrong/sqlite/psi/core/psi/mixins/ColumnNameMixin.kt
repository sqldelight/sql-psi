package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.AnnotationException
import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnReference
import com.alecstrong.sqlite.psi.core.psi.SqliteNamedElementImpl
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder

internal abstract class ColumnNameMixin(
    node: ASTNode
) : SqliteNamedElementImpl(node) {
  override val parseRule: (PsiBuilder, Int) -> Boolean = SqliteParser::column_name_real

  override fun getReference() = SqliteColumnReference(this)

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    try {
      val source = reference.unsafeResolve()
      if (source == null) {
        annotationHolder.createErrorAnnotation(this, "No column found with name $name")
      }
    } catch (e: AnnotationException) {
      annotationHolder.createErrorAnnotation(e.element ?: this, e.msg)
    }
  }
}
