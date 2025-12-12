package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.AnnotationException
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlParser
import com.alecstrong.sql.psi.core.psi.SqlColumnReference
import com.alecstrong.sql.psi.core.psi.SqlNamedElementImpl
import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import javax.swing.Icon

internal abstract class ColumnNameMixin(node: ASTNode) : SqlNamedElementImpl(node) {
  override val parseRule: (PsiBuilder, Int) -> Boolean = SqlParser::column_name_real

  override fun getReference() = SqlColumnReference(this)

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    try {
      val source = reference.unsafeResolve()
      if (source == null) {
        annotationHolder.createErrorAnnotation(this, "No column found with name $name")
      }
    } catch (e: AnnotationException) {
      annotationHolder.createErrorAnnotation(e.element ?: this, e.msg)
    }
  }

  override fun getIcon(flags: Int): Icon {
    return AllIcons.Nodes.DataColumn
  }
}
