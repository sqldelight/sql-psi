package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlWithClause
import com.alecstrong.sql.psi.core.psi.asColumns
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class WithClauseContainer(
  node: ASTNode
) : SqlCompositeElementImpl(node) {
  abstract fun getWithClause(): SqlWithClause?

  override fun tablesAvailable(child: PsiElement): Collection<LazyQuery> {
    getWithClause()?.let {
      if (child != it) return super.tablesAvailable(child) + it.tablesExposed()
    }
    return super.tablesAvailable(child)
  }

  protected fun SqlWithClause.tablesExposed(): List<LazyQuery> {
    return cteTableNameList.zip(compoundSelectStmtList)
        .map { (name, selectStmt) ->
          LazyQuery(name.tableName) {
            val query = QueryResult(name.tableName,
                selectStmt.queryExposed().flatMap { it.columns })
            return@LazyQuery if (name.columnAliasList.isNotEmpty()) {
              QueryResult(name.tableName, name.columnAliasList.asColumns())
            } else {
              query
            }
          }
        }
  }
}