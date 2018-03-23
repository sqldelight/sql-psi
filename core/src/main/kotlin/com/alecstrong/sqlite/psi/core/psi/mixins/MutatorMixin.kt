package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteQualifiedTableName
import com.alecstrong.sqlite.psi.core.psi.SqliteWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class MutatorMixin(
    node: ASTNode
) : WithClauseContainer(node) {
  // One of these will get overridden with what we want. If not error! Kind of type safe?
  open fun getQualifiedTableName(): SqliteQualifiedTableName = throw AssertionError()
  open fun getTableName() = getQualifiedTableName().tableName

  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    val tableExposed = tableAvailable(child, getTableName().name)

    return if (child !is SqliteWithClause) {
      super.queryAvailable(child) + tableExposed
    } else {
      super.queryAvailable(child)
    }
  }
}