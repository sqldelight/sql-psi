package com.alecstrong.sql.psi.core.mysql.psi.mixins

import com.alecstrong.sql.psi.core.mysql.psi.MySqlPlacementClause
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryColumn
import com.alecstrong.sql.psi.core.psi.SqlAlterTableStmt
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

internal abstract class PlacementClauseMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    MySqlPlacementClause {
  override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
    if (child == columnName) {
      val alterStmt = PsiTreeUtil.getParentOfType(this, SqlAlterTableStmt::class.java)!!
      return tablesAvailable(this)
          .filter { it.tableName.text == alterStmt.tableName?.text }
          .map { it.query }
    }
    return super.queryAvailable(child)
  }
}

internal fun MySqlPlacementClause?.placeInQuery(
  columns: List<QueryColumn>,
  column: QueryColumn,
  replace: QueryColumn? = null
): List<QueryColumn> {
  if (this == null) {
    return if (replace == null) columns + column
    else columns.map { if (it == replace) column else it }
  }

  return if (columnName != null) {
    // Place the column after the given column.
    columns.toMutableList().apply {
      if (replace != null) remove(replace)

      val index = indexOfFirst { (it.element as SqlColumnName).text == columnName!!.text }
      if (index == -1) throw IllegalStateException("Unable to replace $replace with $column in $columns")
      add(index + 1, column)
    }
  } else {
    // Place column first.
    listOf(column) + columns.filterNot { it == replace }
  }
}

private val MySqlPlacementClause.columnName
  get() = PsiTreeUtil.findChildOfType(this, SqlColumnName::class.java)
