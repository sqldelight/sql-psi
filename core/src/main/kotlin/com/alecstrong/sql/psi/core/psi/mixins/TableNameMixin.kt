package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlParser
import com.alecstrong.sql.psi.core.psi.SqlColumnExpr
import com.alecstrong.sql.psi.core.psi.SqlDropTableStmt
import com.alecstrong.sql.psi.core.psi.SqlForeignTable
import com.alecstrong.sql.psi.core.psi.SqlNamedElementImpl
import com.alecstrong.sql.psi.core.psi.SqlNewTableName
import com.alecstrong.sql.psi.core.psi.SqlTableName
import com.alecstrong.sql.psi.core.psi.SqlTableReference
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.SqlViewName
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiReference

internal abstract class TableNameMixin(
  node: ASTNode
) : SqlNamedElementImpl(node) {
  override val parseRule: (PsiBuilder, Int) -> Boolean
    get() = when (this) {
      is SqlTableName -> SqlParser::table_name_real
      is SqlViewName -> SqlParser::view_name_real
      is SqlForeignTable -> SqlParser::foreign_table_real
      is SqlNewTableName -> SqlParser::new_table_name_real
      else -> throw IllegalStateException("Unknown table type ${this::class}")
    }

  override fun getReference(): PsiReference {
    return SqlTableReference(this)
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    // Handled by ColumnNameMixin
    if (parent is SqlColumnExpr) return

    val matches by lazy { tableAvailable(this, name) }
    val references = reference.resolve()
    if (references == this) {
      if (matches.any { it.table != this }) {
        annotationHolder.createErrorAnnotation(this, "Table already defined with name $name")
      }
    } else if (references == null) {
      if (parent is SqlDropTableStmt && parent.node.findChildByType(SqlTypes.EXISTS) != null) return
      annotationHolder.createErrorAnnotation(this, "No table found with name $name")
    }
    super.annotate(annotationHolder)
  }
}
