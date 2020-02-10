package com.alecstrong.sql.psi.core.psi

import com.intellij.psi.PsiNameIdentifierOwner

interface NamedElement : PsiNameIdentifierOwner {
  override fun getName(): String
}