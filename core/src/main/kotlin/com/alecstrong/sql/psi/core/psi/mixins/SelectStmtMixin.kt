package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.ModifiableFileLazy
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.FromQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlSelectStmt
import com.alecstrong.sql.psi.core.psi.asColumns
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class SelectStmtMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlSelectStmt,
    FromQuery {
  private val queryExposed: Collection<QueryResult> by ModifiableFileLazy(containingFile) {
    if (valuesExpressionList.isNotEmpty()) {
      return@ModifiableFileLazy listOf(QueryResult(null, valuesExpressionList.first().exprList.asColumns()))
    }
    return@ModifiableFileLazy listOf(QueryResult(
        null,
        columns = resultColumnList.flatMap { it.queryExposed().flatMap { it.columns } }
    ))
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child in exprList || child in resultColumnList) {
      return fromQuery().map { it.copy(adjacent = true) } +
          super.queryAvailable(this).map { it.copy(adjacent = false) }
    }
    if (child == joinClause) return super.queryAvailable(child)
    return super.queryAvailable(child)
  }

  override fun queryExposed() = queryExposed

  override fun fromQuery(): Collection<QueryResult> {
    joinClause?.let {
      return it.queryExposed()
    }
    return emptyList()
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)

    if (valuesExpressionList.isNotEmpty()) {
      val size = valuesExpressionList[0].exprList.size
      valuesExpressionList.drop(1).forEach {
        if (it.exprList.size != size) {
          annotationHolder.createErrorAnnotation(it,
              "Unexpected number of columns in values found: ${it.exprList.size} expected: $size")
        }
      }
    }
  }
}
