package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.postgresql.psi.PostgreSqlReturningClause
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlCompoundSelectStmt
import com.alecstrong.sql.psi.core.psi.SqlWithClause
import com.alecstrong.sql.psi.core.psi.asColumns
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

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
    return cteTableNameList.zip(withClauseAuxiliaryStmtList)
      .mapNotNull { (name, withClauseAuxiliaryStmt) ->
        PsiTreeUtil.findChildOfAnyType(withClauseAuxiliaryStmt, SqlCompoundSelectStmt::class.java, PostgreSqlReturningClause::class.java)
          ?.let { name to it }
      }
      .map { (name, queryElement) ->
        LazyQuery(name.tableName) {
          QueryResult(
            name.tableName,
            name.columnAliasList.asColumns().ifEmpty { queryElement.queryExposed().flatMap(QueryResult::columns) }
          )
        }
      }
  }
}
