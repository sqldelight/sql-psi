package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTriggerStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteQualifiedTableName
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

internal abstract class MutatorMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node) {
  abstract fun getWithClause(): SqliteWithClause?

  // One of these will get overridden with what we want. If not error! Kind of type safe?
  open fun getQualifiedTableName(): SqliteQualifiedTableName = throw AssertionError()
  open fun getTableName() = getQualifiedTableName().tableName

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    val commonTable = getWithClause()?.queryExposed()
    val globalTables = PsiTreeUtil.getParentOfType(this, SqlStmtListMixin::class.java)!!
        .queryAvailable(this)
    val tableExposed = globalTables.filter { it.table?.name == getTableName().name }

    if (child is SqliteExpr) {
      val withTables = getWithClause()?.queryExposed() ?: emptyList()
      if (parent is SqliteCreateTriggerStmt) {
        return super.queryAvailable(child) + tableExposed + withTables
      }
      return tableExposed + withTables
    } else {
      return globalTables
    }
  }
}