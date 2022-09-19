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
    val select = compoundSelectStmt
    if (select != null) {
      select.annotate(annotationHolder)
      for (sqlSelectStmt in select.selectStmtList) {
        val selectInto = sqlSelectStmt.selectIntoClause
        if (selectInto != null) {
          annotationHolder.createErrorAnnotation(
            selectInto,
            "Cannot use SET with SELECT INTO",
          )
        }
      }
    }
    setSetterClause?.annotate(annotationHolder)

    val hostVariables = hostVariableList.size
    val resultColumns = queryExposed().sumBy { it.columns.size + it.synthesizedColumns.size }
    if (hostVariables > resultColumns) {
      annotationHolder.createErrorAnnotation(
        this,
        "Cannot bind $hostVariables host variables to $resultColumns result columns",
      )
    }
  }
}
