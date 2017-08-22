package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteQualifiedTableName
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class MutatorMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node) {
  abstract fun getWithClause(): SqliteWithClause?

  // One of these will get overridden with what we want. If not error! Kind of type safe?
  open fun getQualifiedTableName(): SqliteQualifiedTableName = throw AssertionError()
  open fun getTableName() = getQualifiedTableName().tableName

  override fun tablesAvailable(child: PsiElement): List<QueryResult> {
    return super.tablesAvailable(child) + (getWithClause()?.queryExposed() ?: emptyList())
  }

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    val tableExposed = tablesAvailable(child).filter { it.table?.name == getTableName().name }

    return if (child !is SqliteWithClause) {
      super.queryAvailable(child) + tableExposed
    } else {
      super.queryAvailable(child)
    }
  }
}