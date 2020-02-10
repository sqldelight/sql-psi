package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlForeignKeyClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class ForeignKeyClauseMixin(
    node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlForeignKeyClause {
  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child in columnNameList) {
      val table = tablesAvailable(child).firstOrNull { it.tableName.name == foreignTable.name } ?: return emptyList()
      return listOf(table.query)
    }
    return super.queryAvailable(child)
  }
}