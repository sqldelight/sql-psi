package com.alecstrong.sql.psi.core.psi

import com.intellij.psi.PsiElement

interface TableElement : SqlCompositeElement, Queryable, SchemaContributor {
  /**
   * Used for IDE if this table should pop up in auto-completion.
   */
  val synthesized: Boolean get() = false
}

interface Queryable : PsiElement {
  fun tableExposed(): LazyQuery
}
