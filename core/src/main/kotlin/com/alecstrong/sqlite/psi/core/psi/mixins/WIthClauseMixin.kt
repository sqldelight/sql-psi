package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteWithClause
import com.intellij.lang.ASTNode

internal abstract class WithClauseMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteWithClause {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    cteTableNameList.zip(compoundSelectStmtList)
        .forEach { (name, selectStmt) ->
          val query = QueryResult(name.tableName, selectStmt.queryExposed().flatMap { it.columns })
          if (name.columnAliasList.isNotEmpty() && name.columnAliasList.size != query.columns.size) {
            annotationHolder.createErrorAnnotation(name, "Incorrect number of columns")
          }
        }
  }

  override fun queryExposed(): List<QueryResult> {
    return cteTableNameList.zip(compoundSelectStmtList)
        .map { (name, selectStmt) ->
          val query = QueryResult(name.tableName, selectStmt.queryExposed().flatMap { it.columns })
          return@map if (name.columnAliasList.isNotEmpty()) {
            QueryResult(name.tableName, name.columnAliasList)
          } else {
            query
          }
        }
  }
}