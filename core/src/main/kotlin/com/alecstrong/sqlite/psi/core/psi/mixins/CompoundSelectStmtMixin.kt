package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCompoundSelectStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteOrderingTerm
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract internal class CompoundSelectStmtMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCompoundSelectStmt {
  override fun queryExposed() = selectStmtList.first().queryExposed()

  override fun tablesAvailable(child: PsiElement): List<QueryResult> {
    return super.tablesAvailable(child) + commonTableExpressionList.flatMap { it.queryExposed() }
  }

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child is SqliteOrderingTerm || child is SqliteExpr) {
      return selectStmtList.first().queryExposed()
    }
    return super.queryAvailable(child)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    val numColumns = selectStmtList[0].queryExposed().flatMap { it.columns }.count()
    selectStmtList.drop(1)
        .forEach {
          val count = it.queryExposed().flatMap { it.columns }.count()
          if (count != numColumns) {
            annotationHolder.createErrorAnnotation(it, "Unexpected number of columns in compound" +
                " statement found: $count expected: $numColumns")
          }
        }
  }
}