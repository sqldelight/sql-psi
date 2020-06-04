package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.AnnotationException
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.NamedElement
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.SqlAlterTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.withAlterStatement
import com.intellij.lang.ASTNode

internal abstract class AlterTableMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlAlterTableStmt,
    TableElement {
  override fun name(): NamedElement = alterTableRulesList
      .mapNotNull { it.alterTableRenameTable?.newTableName }
      .lastOrNull() ?: tableName!!

  override fun tableExposed(): LazyQuery {
    return LazyQuery(
        tableName = name(),
        query = result@{
          val tableName = (tableName?.reference?.resolve()?.parent as? TableElement)
              ?: return@result QueryElement.QueryResult(columns = emptyList())
          val lazyQuery = tableName.tableExposed()
          try {
            lazyQuery.withAlterStatement(this)
          } catch (e: AnnotationException) {
            lazyQuery
          }.query
        }
    )
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    when (tableName?.reference?.resolve()?.parent) {
      is AlterTableMixin, is CreateTableMixin -> {}
      else -> {
        annotationHolder.createErrorAnnotation(
            tableName ?: this,
            "Attempting to alter something that is not a table."
        )
        return
      }
    }
    try {
      (tableName!!.reference!!.resolve()!!.parent as TableElement)
          .tableExposed()
          .withAlterStatement(this)
    } catch (e: AnnotationException) {
      annotationHolder.createErrorAnnotation(e.element ?: this, e.msg)
    }
    super.annotate(annotationHolder)
  }
}
