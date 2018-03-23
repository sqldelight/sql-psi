package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteSelectStmt
import com.alecstrong.sqlite.psi.core.psi.asColumns
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class SelectStmtMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteSelectStmt {
  override fun queryAvailable(child: PsiElement): List<QueryResult> = analyze("queryAvailable") {
    if (child in resultColumnList) return fromQuery()
    if (child in exprList) {
      return fromQuery().map { it.copy(adjacent = true) } +
          super.queryAvailable(this).map { it.copy(adjacent = false) }
    }
    if (child == joinClause) return super.queryAvailable(child)
    return super.queryAvailable(child)
  }

  override fun queryExposed(): List<QueryResult> = analyze("queryExposed") {
    if (valuesExpressionList.isNotEmpty()) {
      return listOf(QueryResult(null, valuesExpressionList.first().exprList.asColumns()))
    }
    return listOf(QueryResult(
        null,
        columns = resultColumnList.flatMap { it.queryExposed().flatMap { it.columns } }
    ))
  }

  internal fun fromQuery(): List<QueryResult> = analyze("fromQuery") {
    joinClause?.let {
      return it.queryExposed()
    }
    return emptyList()
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
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