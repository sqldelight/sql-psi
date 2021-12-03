package com.alecstrong.sql.psi.core.psi

import com.intellij.psi.PsiElement

interface AliasElement : PsiElement, NamedElement {
  fun source(): PsiElement
}
