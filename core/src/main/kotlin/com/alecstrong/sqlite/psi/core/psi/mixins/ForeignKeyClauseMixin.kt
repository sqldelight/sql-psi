package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteForeignKeyClause
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class ForeignKeyClauseMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteForeignKeyClause {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child in columnNameList) {
      return tablesAvailable(child).filter { it.table?.name == foreignTable.name }
    }
    return super.queryAvailable(child)
  }
}