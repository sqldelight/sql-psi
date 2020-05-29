package com.alecstrong.sql.psi.core.psi

internal interface TableElement : SqlCompositeElement {
  fun tableExposed(): LazyQuery
  fun name(): NamedElement
}
