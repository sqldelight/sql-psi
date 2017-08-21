package com.alecstrong.sqlite.psi.core.psi

import com.intellij.psi.PsiElement

internal interface SqliteAliasElement: SqliteCompositeElement {
  fun source(): PsiElement
}