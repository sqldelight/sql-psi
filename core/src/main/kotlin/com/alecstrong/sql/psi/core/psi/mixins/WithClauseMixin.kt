package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

internal abstract class WithClauseMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlWithClause {
  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    cteTableNameList.zip(withClauseAuxiliaryStmtList)
        .mapNotNull { (name, withClauseAuxiliaryStmt) ->
          PsiTreeUtil.findChildOfAnyType(withClauseAuxiliaryStmt, QueryElement::class.java)?.let { name to it }
        }
        .forEach { (name, queryElement) ->
          val query = QueryResult(name.tableName, queryElement.queryExposed().flatMap { it.columns })
          if (name.columnAliasList.isNotEmpty() && name.columnAliasList.size != query.columns.size) {
            annotationHolder.createErrorAnnotation(name, "Incorrect number of columns")
          }
        }
  }
}
