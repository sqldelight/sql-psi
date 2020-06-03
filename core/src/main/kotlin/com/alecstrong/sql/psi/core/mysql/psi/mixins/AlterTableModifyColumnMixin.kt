package com.alecstrong.sql.psi.core.mysql.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.mysql.psi.MySqlAlterTableModifyColumn
import com.alecstrong.sql.psi.core.psi.AlterTableApplier
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.SqlAlterTableStmt
import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

internal abstract class AlterTableModifyColumnMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    MySqlAlterTableModifyColumn,
    AlterTableApplier {
  private val columnDef
    get() = children.filterIsInstance<SqlColumnDef>().single()

  override fun applyTo(lazyQuery: LazyQuery): LazyQuery {
    return LazyQuery(
        tableName = lazyQuery.tableName,
        query = {
          lazyQuery.query.copy(
              columns = lazyQuery.query.columns.map {
                if ((it.element as SqlColumnName).text == columnDef.columnName.text) {
                  it.copy(element = columnDef.columnName)
                } else {
                  it
                }
              }
          )
        }
    )
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)
    val modifyingTable = PsiTreeUtil.getParentOfType(
        this,
        SqlAlterTableStmt::class.java
    )!!.tableName

    if (tablesAvailable(this)
        .filter { it.tableName.text == modifyingTable?.text }
        .flatMap { it.query.columns }
        .none { (it.element as? SqlColumnName)?.text == columnDef.columnName.text }) {
      annotationHolder.createErrorAnnotation(
          element = columnDef.columnName,
          s = "No column found to modify with name ${columnDef.columnName.text}"
      )
    }
  }
}
