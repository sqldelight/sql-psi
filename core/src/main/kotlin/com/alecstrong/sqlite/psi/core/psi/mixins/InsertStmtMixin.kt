package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class InsertStmtMixin(
    node: ASTNode
) : MutatorMixin(node),
    SqliteInsertStmt {
  override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
    // Aliasing the table in an insert is useful when doing an UPSERT operation:
    // INSERT INTO tbl AS tblAlias (..) VALUES (..) ON CONFLICT (..) DO UPDATE SET x = tblAlias.x + excluded.x
    //                    ^^^^^^^^                                                     ^^^^^^^^
    tableAlias?.let { alias ->
      val available = ArrayList(super.queryAvailable(child))
      val tableResult = available.find { it.table?.name == tableName.name }
      check(tableResult != null)
      available.remove(tableResult)
      available += tableResult.copy(table = alias)
      return available
    }

    return super.queryAvailable(child)
  }
}