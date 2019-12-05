package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.*
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class UpsertClauseMixin(
        node: ASTNode
) : SqliteCompositeElementImpl(node),
        SqliteUpsertClause {

    override fun tablesAvailable(child: PsiElement): Collection<LazyQuery> {
        return super.tablesAvailable(child)
    }
    override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
        val insertStmt = (this.parent as InsertStmtMixin)
        val tableName = insertStmt.tableName
        val table = tablesAvailable(this).first { it.tableName.name == tableName.name }.query

        if (child is SqliteUpsertConflictTarget) {
            return super.queryAvailable(child)
        }

        if (child is SqliteUpsertDoUpdate) {
            // TODO: Ensure the table alias is available here too (and add it to the grammar)
            val excludedTable = QueryElement.QueryResult(SingleRow(tableName, "excluded"), table.columns, synthesizedColumns = table.synthesizedColumns)
            return super.queryAvailable(child) + listOf(excludedTable)
        }

        return super.queryAvailable(child)
    }
}