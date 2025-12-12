package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCreateTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlQualifiedTableName
import com.alecstrong.sql.psi.core.psi.SqlUpdateStmt
import com.alecstrong.sql.psi.core.psi.SqlUpdateStmtLimited
import com.alecstrong.sql.psi.core.psi.SqlViewName
import com.alecstrong.sql.psi.core.psi.SqlWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class MutatorMixin(node: ASTNode) : WithClauseContainer(node) {
  // One of these will get overridden with what we want. If not error! Kind of type safe?
  open fun getQualifiedTableName(): SqlQualifiedTableName = throw AssertionError()

  open fun getTableName() = getQualifiedTableName().tableName

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    val tableExposed = tableAvailable(child, getTableName().name)

    return if (child !is SqlWithClause) {
      super.queryAvailable(child) + tableExposed
    } else {
      super.queryAvailable(child)
    }
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)
    val tables =
      tableAvailable(this, getTableName().name).ifEmpty {
        return
      }
    val tableUpdated = tables.singleOrNull()?.table
    if (tableUpdated == null) {
      annotationHolder.createErrorAnnotation(
        getTableName(),
        "Trying to mutate something which is not mutable.",
      )
      return
    }

    if ((this is SqlUpdateStmt || this is SqlUpdateStmtLimited) && tableUpdated is SqlViewName) {
      // Find the trigger that does INSTEAD OF UPDATE.
      val trigger =
        containingFile.schema<SqlCreateTriggerStmt>().find {
          it.tableName?.name == tableUpdated.name &&
            it.node.getChildren(null).map { it.text }.containsAll(listOf("INSTEAD", "OF", "UPDATE"))
        }
      if (trigger == null) {
        annotationHolder.createErrorAnnotation(
          getTableName(),
          "Cannot UPDATE the view ${tableUpdated.text} without a trigger on ${tableUpdated.text} that has INSTEAD OF UPDATE.",
        )
        return
      }
    }
  }
}
