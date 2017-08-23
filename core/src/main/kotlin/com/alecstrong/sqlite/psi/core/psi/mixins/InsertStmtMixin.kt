package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnDef
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnName
import com.alecstrong.sqlite.psi.core.psi.SqliteInsertStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.lang.ASTNode

internal abstract class InsertStmtMixin(
    node: ASTNode
) : MutatorMixin(node),
    SqliteInsertStmt {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    val table = tablesAvailable(this).firstOrNull { it.table?.name == tableName.name } ?: return
    val columns = table.columns.map { (it as SqliteColumnName).name!! }
    val setColumns = if (columnNameList.isEmpty()) columns else columnNameList.mapNotNull { it.name }

    valuesExpressionList.forEach {
      if (it.exprList.size != setColumns.size) {
        annotationHolder.createErrorAnnotation(it, "Unexpected number of values being inserted." +
            " found: ${it.exprList.size} expected: ${setColumns.size}")
      }
    }

    compoundSelectStmt?.let { select ->
      val size = select.queryExposed().flatMap { it.columns }.count()
      if (size != setColumns.size) {
        annotationHolder.createErrorAnnotation(select, "Unexpected number of values being" +
            " inserted. found: $size expected: ${setColumns.size}")
      }
    }

    table.columns
        .filterIsInstance<SqliteColumnName>()
        .filterNot { it.name!! in setColumns }
        .forEach {
          if (!(it.parent as SqliteColumnDef).hasDefaultValue()) {
            annotationHolder.createErrorAnnotation(this, "Cannot populate default value for" +
                " column ${it.name}, it must be specified in insert statement.")
          }
        }

    super.annotate(annotationHolder)
  }

  companion object {
    private fun SqliteColumnDef.hasDefaultValue(): Boolean {
      return columnConstraintList.any {
        it.node.findChildByType(SqliteTypes.DEFAULT) != null
            || it.node.findChildByType(SqliteTypes.AUTOINCREMENT) != null
      }
    }
  }
}