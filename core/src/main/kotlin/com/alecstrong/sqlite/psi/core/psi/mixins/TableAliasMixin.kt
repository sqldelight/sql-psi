package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteTableAlias
import com.alecstrong.sqlite.psi.core.psi.SqliteTableOrSubquery
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class TableAliasMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteTableAlias {
  private var hardcodedName: String? = null

  override fun getName(): String = hardcodedName ?: text
  override fun setName(name: String) = apply { hardcodedName = name }
  override fun source(): PsiElement {
    return (parent as SqliteTableOrSubquery).let { it.tableName ?: it.compoundSelectStmt!! }
  }
}