package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.alecstrong.sqlite.psi.core.psi.SqliteNamedElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteTableAlias
import com.alecstrong.sqlite.psi.core.psi.SqliteTableOrSubquery
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiElement

internal abstract class TableAliasMixin(
    node: ASTNode
) : SqliteNamedElementImpl(node),
    SqliteTableAlias {
  override val parseRule: (PsiBuilder, Int) -> Boolean = SqliteParser::table_alias_real

  override fun source(): PsiElement {
    return (parent as SqliteTableOrSubquery).let { it.tableName ?: it.compoundSelectStmt!! }
  }
}