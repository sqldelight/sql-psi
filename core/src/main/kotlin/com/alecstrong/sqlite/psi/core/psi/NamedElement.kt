package com.alecstrong.sqlite.psi.core.psi

import com.intellij.psi.PsiNameIdentifierOwner

interface NamedElement : PsiNameIdentifierOwner {
  override fun getName(): String
}