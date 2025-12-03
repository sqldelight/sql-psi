package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlParser
import com.alecstrong.sql.psi.core.psi.SqlNamedElementImpl
import com.alecstrong.sql.psi.core.psi.SqlTableAlias
import com.alecstrong.sql.psi.core.psi.SqlTableOrSubquery
import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiElement
import javax.swing.Icon

internal abstract class TableAliasMixin(node: ASTNode) : SqlNamedElementImpl(node), SqlTableAlias {
  override val parseRule: (PsiBuilder, Int) -> Boolean = SqlParser::table_alias_real

  override fun source(): PsiElement {
    return (parent as SqlTableOrSubquery).let { it.tableName ?: it.compoundSelectStmt!! }
  }

  override fun getIcon(flags: Int): Icon {
    return AllIcons.Nodes.DataTables
  }
}
