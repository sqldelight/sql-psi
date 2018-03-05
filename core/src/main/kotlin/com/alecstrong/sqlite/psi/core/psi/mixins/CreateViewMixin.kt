package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
import com.alecstrong.sqlite.psi.core.psi.TableElement
import com.intellij.lang.ASTNode

internal abstract class CreateViewMixin(
  node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateViewStmt,
    TableElement {
  override fun tableExposed() = LazyQuery(viewName) {
    QueryResult(viewName, compoundSelectStmt.queryExposed().flatMap { it.columns })
  }
}