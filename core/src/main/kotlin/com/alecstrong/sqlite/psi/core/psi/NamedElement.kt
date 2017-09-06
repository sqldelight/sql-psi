package com.alecstrong.sqlite.psi.core.psi

import com.intellij.psi.PsiNamedElement

interface NamedElement : PsiNamedElement {
  override fun getName(): String
}