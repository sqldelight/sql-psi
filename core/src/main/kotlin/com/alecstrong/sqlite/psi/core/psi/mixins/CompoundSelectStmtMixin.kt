package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.ModifiableFileLazy
import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompoundSelectStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateViewStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteOrderingTerm
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.alecstrong.sqlite.psi.core.psi.SqliteWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

abstract internal class CompoundSelectStmtMixin(
    node: ASTNode
) : WithClauseContainer(node),
    SqliteCompoundSelectStmt {
  private val queryExposed: Collection<QueryResult> by ModifiableFileLazy(containingFile) {
    if (detectRecursion() != null) {
      return@ModifiableFileLazy emptyList<QueryResult>()
    }
    if (parent is SqliteWithClause) {
      // Compound information not needed.
      return@ModifiableFileLazy selectStmtList.first().queryExposed()
    }
    return@ModifiableFileLazy selectStmtList.drop(1).fold(selectStmtList.first().queryExposed()) { query, compounded ->
      val columns = query.flatMap { it.columns }
      val compoundedColumns = compounded.queryExposed().flatMap { it.columns }
      return@fold listOf(query.first().copy(
          columns = columns.zip(compoundedColumns) { column, compounded ->
            column.copy(compounded = column.compounded + compounded)
          }
      ))
    }
  }

  override fun queryExposed() = queryExposed

  override fun tablesAvailable(child: PsiElement): Collection<LazyQuery> {
    val tablesAvailable = super.tablesAvailable(child)
    val parent = parent
    if (parent is SqliteWithClause) {
      if (parent.node.findChildByType(SqliteTypes.RECURSIVE) != null
          && child != selectStmtList.first()) {
        return tablesAvailable + parent.tablesExposed()
      }
      val myIndex = parent.compoundSelectStmtList.indexOf(this)
      return tablesAvailable + parent.tablesExposed().filterIndexed { index, _ -> index != myIndex }
    }
    return tablesAvailable
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child is SqliteExpr) {
      return queryExposed()
    } else if (child is SqliteOrderingTerm) {
      val exposed = queryExposed()
      val exposedColumns = exposed.flatMap { it.columns }
      // Ordering terms are also applicable in the select statement's from clause.
      return (selectStmtList.first() as SelectStmtMixin).fromQuery().filter { it !in exposed }
          .map { QueryResult(it.table, it.columns.filter { it !in exposedColumns }) }
          .plus(exposed)
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

    val viewTree = linkedSetOf(view.viewName.name)

    fun SqliteCreateViewStmt.recursion(): String? {
      PsiTreeUtil.findChildrenOfType(compoundSelectStmt, TableNameMixin::class.java).forEach {
        val name = it.name
        if (!viewTree.add(name)) {
          return viewTree.joinToString(" -> ") + " -> $name"
        }
        containingFile.viewForName(name)?.recursion()?.let { return it }
        viewTree.remove(name)
      }
      return null
    }

    return view.recursion()
  }
}