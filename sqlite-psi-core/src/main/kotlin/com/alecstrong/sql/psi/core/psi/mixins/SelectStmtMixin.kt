package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.ModifiableFileLazy
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.FromQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlBinaryAndExpr
import com.alecstrong.sql.psi.core.psi.SqlBinaryOrExpr
import com.alecstrong.sql.psi.core.psi.SqlBindExpr
import com.alecstrong.sql.psi.core.psi.SqlColumnAlias
import com.alecstrong.sql.psi.core.psi.SqlColumnExpr
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlExpr
import com.alecstrong.sql.psi.core.psi.SqlIsExpr
import com.alecstrong.sql.psi.core.psi.SqlLiteralExpr
import com.alecstrong.sql.psi.core.psi.SqlParenExpr
import com.alecstrong.sql.psi.core.psi.SqlSelectStmt
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.asColumns
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

internal abstract class SelectStmtMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
  SqlSelectStmt,
  FromQuery {
  private val queryExposed = ModifiableFileLazy {
    if (valuesExpressionList.isNotEmpty()) {
      return@ModifiableFileLazy listOf(QueryResult(null, valuesExpressionList.first().exprList.asColumns()))
    }
    return@ModifiableFileLazy listOf(
      QueryResult(
        null,
        columns = resultColumnList.flatMap { resultColumn ->
          resultColumn.queryExposed().flatMap { queryResult ->
            queryResult.columns.map {
              if (exprList.size > 0 && it.element.nonNullIn(exprList[0])) it.copy(nullable = false)
              else it
            }
          }
        }
      )
    )
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child in exprList || child in resultColumnList) {
      return fromQuery().map { it.copy(adjacent = true) } +
        super.queryAvailable(this).map { it.copy(adjacent = false) }
    }
    if (child == joinClause) return super.queryAvailable(child)
    return super.queryAvailable(child)
  }

  override fun queryExposed() = queryExposed.forFile(containingFile)

  override fun fromQuery(): Collection<QueryResult> {
    joinClause?.let {
      return it.queryExposed()
    }
    return emptyList()
  }

  private fun PsiElement.nonNullIn(whereExpr: SqlExpr): Boolean {
    if (this is SqlColumnAlias) return source().nonNullIn(whereExpr)
    if (this is SqlColumnExpr) return columnName.nonNullIn(whereExpr)
    if (this !is SqlColumnName) return false
    return when (whereExpr) {
      is SqlParenExpr -> nonNullIn(whereExpr.expr ?: return false)
      is SqlIsExpr -> {
        val (lhs, rhs) = whereExpr.exprList
        (lhs is SqlColumnExpr && lhs.columnName.isSameAs(this)) &&
          (rhs is SqlLiteralExpr && rhs.literalValue.node.findChildByType(SqlTypes.NULL) != null) &&
          whereExpr.node.findChildByType(SqlTypes.NOT) != null
      }
      is SqlBinaryAndExpr -> nonNullIn(whereExpr.getExprList()[0]) || nonNullIn(whereExpr.getExprList()[1])
      is SqlBinaryOrExpr -> nonNullIn(whereExpr.getExprList()[0]) && nonNullIn(whereExpr.getExprList()[1])
      else -> false
    }
  }

  private fun SqlColumnName.isSameAs(other: SqlColumnName): Boolean {
    if (this == other) return true
    val thisRef = reference?.resolve() ?: return false
    if (thisRef == other) return true
    val otherRef = other.reference?.resolve() ?: return false
    return thisRef == otherRef
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)

    val invalidGroupByBindExpression = exprList.find { child ->
      child is SqlBindExpr &&
        PsiTreeUtil.findSiblingBackward(child, SqlTypes.HAVING, null) == null &&
        PsiTreeUtil.findSiblingBackward(child, SqlTypes.BY, null) != null &&
        PsiTreeUtil.findSiblingBackward(child, SqlTypes.GROUP, null) != null
    }
    if (invalidGroupByBindExpression != null) {
      annotationHolder.createErrorAnnotation(
        invalidGroupByBindExpression,
        "Cannot bind the name of a column in a GROUP BY clause"
      )
    }

    if (valuesExpressionList.isNotEmpty()) {
      val size = valuesExpressionList[0].exprList.size
      valuesExpressionList.drop(1).forEach {
        if (it.exprList.size != size) {
          annotationHolder.createErrorAnnotation(
            it,
            "Unexpected number of columns in values found: ${it.exprList.size} expected: $size"
          )
        }
      }
    }
  }
}
