package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnAlias
import com.alecstrong.sqlite.psi.core.psi.SqliteCompoundSelectStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCteTableName
import com.alecstrong.sqlite.psi.core.psi.SqliteNamedElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteWithClause
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiElement

internal abstract class ColumnAliasMixin(
    node: ASTNode
) : SqliteNamedElementImpl(node),
    SqliteColumnAlias {
  override val parseRule: (PsiBuilder, Int) -> Boolean = SqliteParser::column_alias_real

  override fun source(): PsiElement = analyze("source") {
    parent.let {
      return when (it) {
        is ResultColumnMixin -> it.expr!!

        is SqliteCteTableName -> {
          val index = it.columnAliasList.indexOf(this)
          it.selectStatement().queryExposed().flatMap { it.columns }.map { it.element }.get(index)
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