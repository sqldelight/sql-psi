package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteColumnAlias
import com.alecstrong.sqlite.psi.core.psi.SqliteCommonTableExpression
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCompoundSelectStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCteTableName
import com.alecstrong.sqlite.psi.core.psi.SqliteWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class ColumnAliasMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteColumnAlias {
  private var hardcodedName: String? = null

  override fun getName(): String = hardcodedName ?: text
  override fun setName(name: String) = apply { hardcodedName = name }
  override fun source(): PsiElement {
    parent.let {
      return when (it) {
        is ResultColumnMixin -> it.expr!!

        is SqliteCteTableName -> {
          val index = it.columnAliasList.indexOf(this)
          it.selectStatement().queryExposed().flatMap { it.columns }.get(index)
        }

        is SqliteCommonTableExpression -> {
          val index = it.columnAliasList.indexOf(this)
          it.compoundSelectStmt.queryExposed().flatMap { it.columns }.get(index)
        }

        else -> throw IllegalStateException("Unexpected column alias parent $it")
      }
    }
  }

  private fun SqliteCteTableName.selectStatement(): SqliteCompoundSelectStmt {
    val withClause = parent as SqliteWithClause
    return withClause.compoundSelectStmtList[withClause.cteTableNameList.indexOf(this)]
  }
}