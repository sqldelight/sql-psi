package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.hasDefaultValue
import com.alecstrong.sqlite.psi.core.psi.*
import com.alecstrong.sqlite.psi.core.psi.impl.SqliteInsertStmtValuesImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class InsertStmtValuesMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteInsertStmtValues {
  override fun getParent(): SqliteInsertStmt {
    return super.getParent() as SqliteInsertStmt
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    val table = tableAvailable(this, parent.tableName.name).firstOrNull() ?: return
    val columns = table.columns.map { (it.element as SqliteColumnName).name }
    // DEFAULT VALUES clause
    val insertDefaultValues = node.findChildByType(SqliteTypes.DEFAULT) != null
    val setColumns =
        if (parent.columnNameList.isEmpty() && !insertDefaultValues) {
          columns
        } else {
          parent.columnNameList.mapNotNull { it.name }
        }

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

    val needsDefaultValue = table.columns
        .filterNot { (element, _) -> element is SqliteColumnName && element.name in setColumns }
        .map { it.element as SqliteColumnName }
        .filterNot { (it.parent as SqliteColumnDef).hasDefaultValue() }
    if (needsDefaultValue.size == 1) {
      annotationHolder.createErrorAnnotation(parent, "Cannot populate default value for column " +
          "${needsDefaultValue.first().name}, it must be specified in insert statement.")
    } else if (needsDefaultValue.size > 1) {
      annotationHolder.createErrorAnnotation(parent, "Cannot populate default values for columns " +
          "(${needsDefaultValue.joinToString { it.name }}), they must be specified in insert statement.")
    }

    super.annotate(annotationHolder)
  }
}