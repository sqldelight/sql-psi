package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlColumnName
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlCreateTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlExpr
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

internal abstract class CreateTriggerMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlCreateTriggerStmt {
  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    if (child is MutatorMixin || child is SqlExpr || child is CompoundSelectStmtMixin) {
      val table = tablesAvailable(this).first { it.tableName.name == tableName?.name }.query
      if (hasElement(SqlTypes.INSERT)) {
        return listOf(QueryResult(SingleRow(tableName!!, "new"), table.columns, synthesizedColumns = table.synthesizedColumns))
      }
      if (hasElement(SqlTypes.UPDATE)) {
        return listOf(QueryResult(SingleRow(tableName!!, "new"), table.columns, synthesizedColumns = table.synthesizedColumns),
            QueryResult(SingleRow(tableName!!, "old"), table.columns, synthesizedColumns = table.synthesizedColumns))
      }
      if (hasElement(SqlTypes.DELETE)) {
        return listOf(QueryResult(SingleRow(tableName!!, "old"), table.columns, synthesizedColumns = table.synthesizedColumns))
      }
    }
    if (child is SqlColumnName) {
      return listOfNotNull(tablesAvailable(this).firstOrNull { it.tableName.name == tableName?.name }?.query)
    }
    return super.queryAvailable(child)
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    if (containingFile.triggers(this).any { it != this && it.triggerName.text == triggerName.text }) {
      annotationHolder.createErrorAnnotation(triggerName,
          "Duplicate trigger name ${triggerName.text}")
    }
  }

  private fun hasElement(elementType: IElementType): Boolean {
    val child = node.findChildByType(elementType) ?: return false
    return child.treeParent == node
  }
}
