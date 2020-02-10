package com.alecstrong.sql.psi.core.sqlite_3_24.psi.mixins

import com.alecstrong.sql.psi.core.psi.*
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.mixins.InsertStmtMixin
import com.alecstrong.sql.psi.core.psi.mixins.SingleRow
import com.alecstrong.sql.psi.core.sqlite_3_24.psi.UpsertClause
import com.alecstrong.sql.psi.core.sqlite_3_24.psi.UpsertConflictTarget
import com.alecstrong.sql.psi.core.sqlite_3_24.psi.UpsertDoUpdate
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class UpsertClauseMixin(
        node: ASTNode
) : SqlCompositeElementImpl(node),
    UpsertClause {

    override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
        val insertStmt = (this.parent as InsertStmtMixin)
        val tableName = insertStmt.tableName
        val table = tablesAvailable(this).first { it.tableName.name == tableName.name }.query

        if (child is UpsertConflictTarget) {
            return super.queryAvailable(child)
        }

        if (child is UpsertDoUpdate) {
            val excludedTable = QueryElement.QueryResult(
                SingleRow(
                    tableName, "excluded"
                ), table.columns,
                    synthesizedColumns = table.synthesizedColumns)

            val available = arrayListOf(excludedTable)
            available += super.queryAvailable(child)
            return available
        }

        return super.queryAvailable(child)
    }
}