package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteBinaryLikeExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnName
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateVirtualTableStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

internal abstract class BinaryLikeExprMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteBinaryLikeExpr {

  private val hasMatchOperator: Boolean
    get() = binaryLikeOperator.node.findChildByType(SqliteTypes.MATCH) != null

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    if (hasMatchOperator) {
      checkForMatchUsageError(annotationHolder)
    }
  }

  /**
   * Check for common cases where the MATCH operator would fail. For example, the left hand side of the MATCH operator
   * must be a column in an FTS table and that table must not be on the right hand side of a LEFT JOIN.
   */
  private fun checkForMatchUsageError(annotationHolder: SqliteAnnotationHolder) {
    val isMatchUsageError = when (val firstExpression = exprList.first()) {
      is SqliteColumnExpr -> {
        when (val resolvedReference = firstExpression.columnName.reference?.resolve()) {
          is SqliteCreateVirtualTableStmt ->
            isMatchUsageErrorOnSynthesizedColumn(firstExpression, resolvedReference)
          is SqliteColumnName -> isMatchUsageErrorOnRegularColumn(firstExpression, resolvedReference)
          null -> false  // Column is invalid, which is a different error that's handled by the column name element
          else -> true
        }
      }
      else -> true
    }

    if (isMatchUsageError) {
      annotationHolder.createErrorAnnotation(
          this,
          "Unable to use function MATCH in the requested context"
      )
    }
  }

  private fun isMatchUsageErrorOnSynthesizedColumn(
      expression: SqliteColumnExpr,
      table: SqliteCreateVirtualTableStmt
  ): Boolean {
    return if (table.usesFtsModule) {
      queryAvailable(expression)
          .filter { it.table?.name == table.tableName.name }
          .any { query -> query.synthesizedColumns.any { it.nullable } }
    } else {
      true
    }
  }

  private fun isMatchUsageErrorOnRegularColumn(
      expression: SqliteColumnExpr,
      columnName: SqliteColumnName
  ): Boolean {
    val createVirtualTableStatement = PsiTreeUtil.findFirstParent(columnName) { it is SqliteCreateVirtualTableStmt }
        as? SqliteCreateVirtualTableStmt

    return if (createVirtualTableStatement != null && createVirtualTableStatement.usesFtsModule) {
      queryAvailable(expression)
          .filter { it.table?.name == createVirtualTableStatement.tableName.name }
          .any { query ->
            query.columns.filter { it.element.text == expression.columnName.name }.any { it.nullable }
          }
    } else {
      true
    }
  }
}