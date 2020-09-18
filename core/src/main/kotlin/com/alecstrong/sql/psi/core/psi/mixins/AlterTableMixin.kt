package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.AnnotationException
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.NamedElement
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SqlAlterTableRules
import com.alecstrong.sql.psi.core.psi.SqlAlterTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.removeTableForName
import com.alecstrong.sql.psi.core.psi.withAlterStatement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class AlterTableMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlAlterTableStmt,
    TableElement {
  override fun name(): NamedElement = alterTableRulesList
      .mapNotNull { it.alterTableRenameTable?.newTableName }
      .lastOrNull() ?: tableName!!

  override fun modifySchema(schema: Schema) {
    tableName?.let { tableName ->
      schema.forType<TableElement, LazyQuery>().removeTableForName(tableName)
      schema.forType<TableElement, LazyQuery>().putValue(this, tableExposed())
    }
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
    if (child in alterTableRulesList) {
      check(child is SqlAlterTableRules)
      return tablesAvailable(this)
          .filter { it.tableName.text == tableName!!.text }
          .map { it.withAlterStatement(this, until = child).query }
    }
    return super.queryAvailable(child)
  }

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
    if (containingFile.order == null) {
      annotationHolder.createErrorAnnotation(this, "Alter table statements are forbidden outside of migration files.")
      return
    }
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
