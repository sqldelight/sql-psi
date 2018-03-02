package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteNamedElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteTableReference
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiReference

internal abstract class TableNameMixin(
    node: ASTNode
) : SqliteNamedElementImpl(node) {
  override val parseRule: (PsiBuilder, Int) -> Boolean = SqliteParser::table_name_real

  override fun getReference(): PsiReference {
    return SqliteTableReference(this)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    // Handled by ColumnNameMixin
    if (parent is SqliteColumnExpr) return

    val matches: List<QueryResult> by lazy { tableAvailable(this, name) }
    val references = reference.resolve()
    if (references == this) {
      if(matches.any { it.table != this }) {
        annotationHolder.createErrorAnnotation(this, "Table already defined with name $name")
      }
    } else if (references == null) {
      annotationHolder.createErrorAnnotation(this, "No table found with name $name")
    }
    super.annotate(annotationHolder)
  }
}