package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.TableElement
import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.asColumns
import com.intellij.lang.ASTNode

internal abstract class CreateViewMixin(
  node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateViewStmt,
    TableElement {

  override fun tableExposed() = LazyQuery(viewName) {
    val columns =
        if (columnAliasList.isEmpty())
          compoundSelectStmt?.queryExposed()?.flatMap { it.columns }
        else
          columnAliasList.asColumns()

    QueryResult(viewName, columns ?: emptyList())
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    super.annotate(annotationHolder)
    if (columnAliasList.isNotEmpty()) {
      if (columnAliasList.size != compoundSelectStmt?.queryExposed()?.map { it.columns }?.size)
        annotationHolder.createErrorAnnotation(this,
            "number of aliases is different from the number of columns")
    }
  }

}