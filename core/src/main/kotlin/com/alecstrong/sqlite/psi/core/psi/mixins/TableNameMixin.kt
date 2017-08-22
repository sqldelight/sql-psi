package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
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
    if (reference.resolve() == this && queryAvailable(this).filter { it.table?.name == name }.size > 1) {
      annotationHolder.createErrorAnnotation(this, "Table already defined with name $name")
    }
    super.annotate(annotationHolder)
  }
}