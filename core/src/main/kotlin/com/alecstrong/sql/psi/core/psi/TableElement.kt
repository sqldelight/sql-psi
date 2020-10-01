package com.alecstrong.sql.psi.core.psi

import com.intellij.psi.PsiElement

internal interface TableElement : SqlCompositeElement, Queryable, SchemaContributor

interface Queryable : PsiElement {
  fun tableExposed(): LazyQuery
}
