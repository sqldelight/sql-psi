package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteTableReference
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference

internal abstract class TableNameMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    PsiNamedElement {
  private var hardcodedName: String? = null

  override fun getName(): String = hardcodedName ?: text
  override fun setName(name: String) = apply { hardcodedName = name }
  override fun getReference(): PsiReference {
    return SqliteTableReference(this)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    // Handled by ColumnNameMixin
    if (parent is SqliteColumnExpr) return

    val matches = tablesAvailable(this).filter { it.table?.name == name }
    if (reference.resolve() == this) {
      if(matches.size > 1) {
        annotationHolder.createErrorAnnotation(this, "Table already defined with name $name")
      }
    } else if (matches.isEmpty()) {
      annotationHolder.createErrorAnnotation(this, "No table found with name $name")
    }
    super.annotate(annotationHolder)
  }
}