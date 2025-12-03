package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlInsertStmt
import com.alecstrong.sql.psi.core.psi.SqlInsertStmtValues
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.lang.ASTNode

internal abstract class InsertStmtValuesMixin(node: ASTNode) :
  SqlCompositeElementImpl(node), SqlInsertStmtValues {
  override fun getParent(): SqlInsertStmt? {
    return super.getParent() as SqlInsertStmt?
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    val parent = parent ?: return
    val table = tableAvailable(this, parent.tableName.name).firstOrNull() ?: return
    val columns = table.columns.map { (it.element as SqlColumnName).name }
    // DEFAULT VALUES clause
    val insertDefaultValues = node.findChildByType(SqlTypes.DEFAULT) != null
    val setColumns =
      if (parent.columnNameList.isEmpty() && !insertDefaultValues) {
        columns
      } else {
        parent.columnNameList.mapNotNull { it.name }
      }

    valuesExpressionList.forEach {
      if (it.exprList.size != setColumns.size) {
        annotationHolder.createErrorAnnotation(
          it,
          "Unexpected number of values being inserted." +
            " found: ${it.exprList.size} expected: ${setColumns.size}",
        )
      }
    }

    compoundSelectStmt?.let { select ->
      val size = select.queryExposed().flatMap { it.columns }.count()
      if (size != setColumns.size) {
        annotationHolder.createErrorAnnotation(
          select,
          "Unexpected number of values being" +
            " inserted. found: $size expected: ${setColumns.size}",
        )
      }
    }

    val needsDefaultValue =
      table.columns
        .filterNot { (element, _) -> element is SqlColumnName && element.name in setColumns }
        .map { it.element as SqlColumnName }
        .filterNot { (it.parent as ColumnDefMixin).hasDefaultValue() }
    if (needsDefaultValue.size == 1) {
      annotationHolder.createErrorAnnotation(
        parent,
        "Cannot populate default value for column " +
          "${needsDefaultValue.first().name}, it must be specified in insert statement.",
      )
    } else if (needsDefaultValue.size > 1) {
      annotationHolder.createErrorAnnotation(
        parent,
        "Cannot populate default values for columns " +
          "(${needsDefaultValue.joinToString { it.name }}), they must be specified in insert statement.",
      )
    }

    super.annotate(annotationHolder)
  }
}
