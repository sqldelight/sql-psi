package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class WithClauseContainer(
  node: ASTNode
) : SqliteCompositeElementImpl(node) {
  abstract fun getWithClause(): SqliteWithClause?

  override fun tablesAvailable(child: PsiElement): List<LazyQuery> {
    getWithClause()?.let {
      if (child != it) return super.tablesAvailable(child) + it.tablesExposed()
    }
    return super.tablesAvailable(child)
  }

  protected fun SqliteWithClause.tablesExposed(): List<LazyQuery> {
    return cteTableNameList.zip(compoundSelectStmtList)
        .map { (name, selectStmt) ->
          LazyQuery(name.tableName) {
            val query = QueryResult(name.tableName,
                selectStmt.queryExposed().flatMap { it.columns })
            return@LazyQuery if (name.columnAliasList.isNotEmpty()) {
              QueryResult(name.tableName, name.columnAliasList)
            } else {
              query
            }
          }
        }
  }
}