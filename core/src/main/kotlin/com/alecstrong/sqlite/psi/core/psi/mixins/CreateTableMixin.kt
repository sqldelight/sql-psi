package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTableStmt
import com.intellij.lang.ASTNode

internal abstract class CreateTableMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateTableStmt {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    columnDefList.map { it.columnName }
        .groupBy { it.name!!.trim('\'', '"', '`', '[', ']') }
        .map { it.value }
        .filter { it.size > 1 }
        .flatMap { it }
        .forEach {
          annotationHolder.createErrorAnnotation(it, "Duplicate column name")
        }

    super.annotate(annotationHolder)
  }
}