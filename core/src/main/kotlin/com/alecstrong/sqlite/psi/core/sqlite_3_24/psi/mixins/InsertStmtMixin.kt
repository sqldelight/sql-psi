package com.alecstrong.sqlite.psi.core.sqlite_3_24.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteInsertStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.alecstrong.sqlite.psi.core.psi.impl.SqliteInsertStmtImpl
import com.alecstrong.sqlite.psi.core.psi.mixins.InsertStmtMixin
import com.alecstrong.sqlite.psi.core.sqlite_3_24.psi.InsertStmt
import com.intellij.lang.ASTNode

internal abstract class InsertStmtMixin(
  node: ASTNode
) : SqliteInsertStmtImpl(node),
    InsertStmt {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    super.annotate(annotationHolder)
    val insertDefaultValues = insertStmtValues?.node?.findChildByType(SqliteTypes.DEFAULT) != null

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
  }
}