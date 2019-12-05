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
    val table = tableAvailable(this, tableName.name).firstOrNull() ?: return
    val columns = table.columns.map { (it.element as SqliteColumnName).name }
    // DEFAULT VALUES clause
    val insertDefaultValues = node.findChildByType(SqliteTypes.DEFAULT) != null
    val setColumns =
        if (columnNameList.isEmpty() && !insertDefaultValues) {
          columns
        } else {
          columnNameList.mapNotNull { it.name }
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
      annotationHolder.createErrorAnnotation(this, "Cannot populate default value for column " +
          "${needsDefaultValue.first().name}, it must be specified in insert statement.")
    } else if (needsDefaultValue.size > 1) {
      annotationHolder.createErrorAnnotation(this, "Cannot populate default values for columns " +
          "(${needsDefaultValue.joinToString { it.name }}), they must be specified in insert statement.")
    }

    upsertClause?.let { upsert ->
      val upsertDoUpdate = upsert.upsertDoUpdate
      if (insertDefaultValues && upsertDoUpdate != null) {
        annotationHolder.createErrorAnnotation(upsert, "The upsert clause is not supported after DEFAULT VALUES")
      }

      val insertOr = node.findChildByType(SqliteTypes.INSERT)?.treeNext
      val replace = node.findChildByType(SqliteTypes.REPLACE)
      val conflictResolution = when {
        replace != null -> SqliteTypes.REPLACE
        insertOr != null && insertOr.elementType == SqliteTypes.OR -> {
          val type = insertOr.treeNext.elementType
          check(type == SqliteTypes.ROLLBACK || type == SqliteTypes.ABORT ||
                  type == SqliteTypes.FAIL || type == SqliteTypes.IGNORE)
          type
        }
        else -> null
      }

      if (conflictResolution != null && upsertDoUpdate != null) {
        annotationHolder.createErrorAnnotation(upsertDoUpdate, "Cannot use DO UPDATE while " +
                "also specifying a conflict resolution algorithm ($conflictResolution)")
      }
    }

    super.annotate(annotationHolder)
  }

  protected fun SqliteColumnDef.hasDefaultValue(): Boolean {
    return columnConstraintList.any {
      it.node.findChildByType(SqliteTypes.DEFAULT) != null
          || it.node.findChildByType(SqliteTypes.AUTOINCREMENT) != null
    } || columnConstraintList.none {
      it.node.findChildByType(SqliteTypes.NOT) != null
    }
  }
}
