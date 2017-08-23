package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCompoundSelectStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteOrderingTerm
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

abstract internal class CompoundSelectStmtMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCompoundSelectStmt {
  override fun queryExposed(): List<QueryResult> {
    if (detectRecursion() != null) {
      return emptyList()
    }
    return selectStmtList.first().queryExposed()
  }

  override fun tablesAvailable(child: PsiElement): List<QueryResult> {
    return super.tablesAvailable(child) + commonTableExpressionList.flatMap { it.queryExposed() }
  }

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child is SqliteOrderingTerm || child is SqliteExpr) {
      return queryExposed()
    }
    return super.queryAvailable(child)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    val numColumns = selectStmtList[0].queryExposed().flatMap { it.columns }.count()

    detectRecursion()?.let { recursion ->
      annotationHolder.createErrorAnnotation(this, "Recursive subquery found: $recursion")
    }

    selectStmtList.drop(1)
        .forEach {
          val count = it.queryExposed().flatMap { it.columns }.count()
          if (count != numColumns) {
            annotationHolder.createErrorAnnotation(it, "Unexpected number of columns in compound" +
                " statement found: $count expected: $numColumns")
          }
        }
  }

  private fun detectRecursion(): String? {
    val view = parent as? SqliteCreateViewStmt ?: return null

    val viewTree = linkedSetOf(view.viewName.name!!)

    fun SqliteCreateViewStmt.recursion(): String? {
      PsiTreeUtil.findChildrenOfType(compoundSelectStmt, TableNameMixin::class.java).forEach {
        val name = it.name
        if (!viewTree.add(name)) {
          return viewTree.joinToString(" -> ") + " -> $name"
        }
        PsiTreeUtil.getParentOfType(this, SqlStmtListMixin::class.java)!!.views()
            .filter { it.viewName.name == name }
            .forEach {
              it.recursion()?.let { return it }
            }
        viewTree.remove(name)
      }
      return null
    }

    return view.recursion()
  }
}