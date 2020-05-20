package com.alecstrong.sql.psi.core.sqlite_3_24.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.impl.SqlInsertStmtImpl
import com.alecstrong.sql.psi.core.sqlite_3_24.psi.InsertStmt
import com.intellij.lang.ASTNode

internal abstract class InsertStmtMixin(
  node: ASTNode
) : SqlInsertStmtImpl(node),
    InsertStmt {
  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)
    val insertDefaultValues = insertStmtValues?.node?.findChildByType(
        SqlTypes.DEFAULT) != null

    upsertClause?.let { upsert ->
      val upsertDoUpdate = upsert.upsertDoUpdate
      if (insertDefaultValues && upsertDoUpdate != null) {
        annotationHolder.createErrorAnnotation(upsert, "The upsert clause is not supported after DEFAULT VALUES")
      }

      val insertOr = node.findChildByType(
          SqlTypes.INSERT)?.treeNext
      val replace = node.findChildByType(
          SqlTypes.REPLACE)
      val conflictResolution = when {
        replace != null -> SqlTypes.REPLACE
        insertOr != null && insertOr.elementType == SqlTypes.OR -> {
          val type = insertOr.treeNext.elementType
          check(type == SqlTypes.ROLLBACK || type == SqlTypes.ABORT ||
              type == SqlTypes.FAIL || type == SqlTypes.IGNORE)
          type
        }
        else -> null
      }

      if (conflictResolution != null && upsertDoUpdate != null) {
        annotationHolder.createErrorAnnotation(upsertDoUpdate, "Cannot use DO UPDATE while " +
            "also specifying a conflict resolution algorithm ($conflictResolution)")
      }
    }
  }
}
