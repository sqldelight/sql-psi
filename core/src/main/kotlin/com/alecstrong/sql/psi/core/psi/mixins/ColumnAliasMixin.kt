package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlParser
import com.alecstrong.sql.psi.core.psi.SqlColumnAlias
import com.alecstrong.sql.psi.core.psi.SqlCompoundSelectStmt
import com.alecstrong.sql.psi.core.psi.SqlCteTableName
import com.alecstrong.sql.psi.core.psi.SqlNamedElementImpl
import com.alecstrong.sql.psi.core.psi.SqlWithClause
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiElement

internal abstract class ColumnAliasMixin(
  node: ASTNode
) : SqlNamedElementImpl(node),
    SqlColumnAlias {
  override val parseRule: (PsiBuilder, Int) -> Boolean = SqlParser::column_alias_real

  override fun source(): PsiElement {
    parent.let {
      return when (it) {
        is ResultColumnMixin -> it.expr!!

        is SqlCteTableName -> {
          val index = it.columnAliasList.indexOf(this)
          it.selectStatement().queryExposed().flatMap { it.columns }.map { it.element }.get(index)
        }

        else -> throw IllegalStateException("Unexpected column alias parent $it")
      }
    }
  }

  private fun SqlCteTableName.selectStatement(): SqlCompoundSelectStmt {
    val withClause = parent as SqlWithClause
    return withClause.compoundSelectStmtList[withClause.cteTableNameList.indexOf(this)]
  }
}
