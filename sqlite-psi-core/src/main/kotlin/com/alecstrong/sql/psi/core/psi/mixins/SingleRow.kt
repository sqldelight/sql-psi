package com.alecstrong.sql.psi.core.psi.mixins

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiNamedElement

internal class SingleRow(
  val originalTable: PsiNamedElement,
  var rowName: String = originalTable.name!!
) : ASTWrapperPsiElement(originalTable.node), PsiNamedElement {
  override fun getName() = rowName
  override fun setName(name: String) = apply { rowName = name }
}
