package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteColumnReference
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement

internal open class ColumnNameMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    PsiNamedElement {
  private var hardcodedName: String? = null

  override fun getReference() = SqliteColumnReference(this)
  override fun getName(): String = hardcodedName ?: text
  override fun setName(name: String) = apply { hardcodedName = name }
}
