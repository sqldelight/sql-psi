package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.SqlSetStmt
import com.alecstrong.sql.psi.core.psi.SqlWithClause
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class SetStmtMixin(
  node: ASTNode,
) : WithClauseContainer(node),
  SqlSetStmt {

  override fun queryAvailable(child: PsiElement): Collection<QueryResult> {
    val compoundSelectStmt = compoundSelectStmt ?: return setSetterClause!!.exprList.map {
      QueryResult(it)
    }
    return if (child in compoundSelectStmt.children) {
      compoundSelectStmt.queryAvailable(child)
    } else emptyList()
  }

  override fun getWithClause(): SqlWithClause? = compoundSelectStmt?.withClause

  override fun queryExposed(): Collection<QueryResult> = compoundSelectStmt?.queryExposed() ?: setSetterClause!!.exprList.map {
    QueryResult(it)
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    super.annotate(annotationHolder)
    compoundSelectStmt?.annotate(annotationHolder)
    setSetterClause?.annotate(annotationHolder)

    val hostVariables = hostVariableList.size
    val setColumns = setSetterClause?.exprList?.size
    val selectColumns = compoundSelectStmt?.selectStmtList?.singleOrNull()?.resultColumnList?.size
    val resultColumns = setColumns ?: selectColumns
    if (resultColumns != null && hostVariables > resultColumns) {
      annotationHolder.createErrorAnnotation(
        this,
        "Cannot bind $hostVariables host variables to $resultColumns result columns",
      )
    }
  }
}
