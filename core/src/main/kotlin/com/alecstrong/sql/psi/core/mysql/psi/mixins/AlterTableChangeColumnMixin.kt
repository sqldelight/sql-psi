package com.alecstrong.sql.psi.core.mysql.psi.mixins

import com.alecstrong.sql.psi.core.mysql.psi.MySqlAlterTableChangeColumn
import com.alecstrong.sql.psi.core.psi.AlterTableApplier
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.alterStmt
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class AlterTableChangeColumnMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    MySqlAlterTableChangeColumn,
    AlterTableApplier {
  private val columnName
      get() = children.filterIsInstance<SqlColumnName>().single()

  private val columnDef
    get() = children.filterIsInstance<SqlColumnDef>().single()

  override fun applyTo(lazyQuery: LazyQuery): LazyQuery {
    return LazyQuery(
        tableName = lazyQuery.tableName,
        query = {
          val columns = placementClause.placeInQuery(
              columns = lazyQuery.query.columns,
              column = QueryElement.QueryColumn(columnDef.columnName),
              replace = lazyQuery.query.columns.single { (it.element as SqlColumnName).text == columnName.text }
          )
          lazyQuery.query.copy(columns = columns)
        }
    )
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
    if (child == columnName) {
      return tablesAvailable(this)
          .filter { it.tableName.text == alterStmt.tableName?.text }
          .map { it.query }
    }
    return super.queryAvailable(child)
  }
}
