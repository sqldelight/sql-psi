package com.alecstrong.sql.psi.core.psi

import com.intellij.psi.PsiElement

interface TableElement : SqlCompositeElement, Queryable, SchemaContributor

interface Queryable : PsiElement {
  fun tableExposed(): LazyQuery
}
