package com.alecstrong.sqlite.psi.core.psi

import com.intellij.psi.PsiElement

interface AliasElement: PsiElement {
  fun source(): PsiElement
}