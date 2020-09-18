package com.alecstrong.sql.psi.core.psi

import com.intellij.psi.PsiElement

internal interface TableElement : SqlCompositeElement, Queryable, SchemaContributor {
  fun name(): NamedElement
}

interface Queryable : PsiElement {
  fun tableExposed(): LazyQuery
}
