package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteForeignTable
import com.alecstrong.sqlite.psi.core.psi.SqliteNamedElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteTableName
import com.alecstrong.sqlite.psi.core.psi.SqliteTableReference
import com.alecstrong.sqlite.psi.core.psi.SqliteViewName
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiReference

internal abstract class TableNameMixin(
    node: ASTNode
) : SqliteNamedElementImpl(node) {
  override val parseRule: (PsiBuilder, Int) -> Boolean
    get() = when (this) {
      is SqliteTableName -> SqliteParser::table_name_real
      is SqliteViewName -> SqliteParser::view_name_real
      is SqliteForeignTable -> SqliteParser::foreign_table_real
      else -> throw IllegalStateException("Unknown table type ${this::class}")
    }

  override fun getReference(): PsiReference {
    return SqliteTableReference(this)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    // Handled by ColumnNameMixin
    if (parent is SqliteColumnExpr) return

    val matches by lazy { tableAvailable(this, name) }
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