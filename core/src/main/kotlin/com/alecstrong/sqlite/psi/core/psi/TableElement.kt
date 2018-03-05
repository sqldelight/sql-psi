package com.alecstrong.sqlite.psi.core.psi

import com.intellij.psi.PsiElement

internal interface TableElement : PsiElement {
  fun tableExposed(): LazyQuery
}