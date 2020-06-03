package com.alecstrong.sql.psi.core.mysql.psi.mixins

import com.alecstrong.sql.psi.core.mysql.psi.MySqlAlterTableChangeColumn
import com.alecstrong.sql.psi.core.psi.AlterTableApplier
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.SqlAlterTableStmt
import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

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
          lazyQuery.query.copy(
              columns = lazyQuery.query.columns.map {
                if ((it.element as SqlColumnName).text == columnName.text) {
                  it.copy(element = columnDef.columnName)
                } else {
                  it
                }
              }
          )
        }
    )
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
    if (child == columnName) {
      val modifyingTable = PsiTreeUtil.getParentOfType(
          this,
          SqlAlterTableStmt::class.java
      )!!.tableName

      return tablesAvailable(this)
          .filter { it.tableName.text == modifyingTable?.text }
          .map { it.query }
    }
    return super.queryAvailable(child)
  }
}
