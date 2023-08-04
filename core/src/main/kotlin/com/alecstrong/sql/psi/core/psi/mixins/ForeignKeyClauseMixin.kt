package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlForeignKeyClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType

internal abstract class ForeignKeyClauseMixin(
  node: ASTNode,
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

fun SqlColumnDef.isForeignKey(): Boolean {
  for (columnConstraint in columnConstraintList) {
    if (columnConstraint.foreignKeyClause != null) {
      return true
    }
  }
  val createTableStmt: SqlCreateTableStmt? = parentOfType()
  if (createTableStmt != null) {
    for (tableConstraints in createTableStmt.tableConstraintList) {
      val foreignTableClause = tableConstraints.foreignTableClause
      if (foreignTableClause != null) {
        val columns = foreignTableClause.columnNameList
        for (column in columns) {
          if (column.name == columnName.name) {
            return true
          }
        }
      }
    }
  }
  return false
}
