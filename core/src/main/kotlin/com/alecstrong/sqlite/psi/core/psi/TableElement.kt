package com.alecstrong.sqlite.psi.core.psi

internal interface TableElement : SqliteCompositeElement {
  fun tableExposed(): LazyQuery
}