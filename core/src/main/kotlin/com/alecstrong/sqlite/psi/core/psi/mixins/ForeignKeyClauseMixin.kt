package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteForeignKeyClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class ForeignKeyClauseMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteForeignKeyClause {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child in columnNameList) {
      val table = tablesAvailable(child).firstOrNull { it.tableName.name == foreignTable.name } ?: return emptyList()
      return listOf(table.query())
    }
    return super.queryAvailable(child)
  }
}