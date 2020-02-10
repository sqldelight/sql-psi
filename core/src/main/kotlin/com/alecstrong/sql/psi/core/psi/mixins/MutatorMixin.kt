package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlQualifiedTableName
import com.alecstrong.sql.psi.core.psi.SqlWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class MutatorMixin(
    node: ASTNode
) : WithClauseContainer(node) {
  // One of these will get overridden with what we want. If not error! Kind of type safe?
  open fun getQualifiedTableName(): SqlQualifiedTableName = throw AssertionError()
  open fun getTableName() = getQualifiedTableName().tableName

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    val tableExposed = tableAvailable(child, getTableName().name)

    return if (child !is SqlWithClause) {
      super.queryAvailable(child) + tableExposed
    } else {
      super.queryAvailable(child)
    }
  }
}