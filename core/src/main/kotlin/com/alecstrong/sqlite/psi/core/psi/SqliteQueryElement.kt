package com.alecstrong.sqlite.psi.core.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

internal interface SqliteQueryElement : SqliteCompositeElement {
  /**
   * Return all of the results that this query exposes. The select_stmt rule
   *
   *   SELECT *
   *   FROM a_table;
   *
   * Would expose [QueryResults(a_table, [all of a_tables columns])]
   *
   * The join_clause rule
   *
   *   a_table JOIN a_second_table
   *
   * Would expose [QueryResult(a_table, [all of a_tables columns]), QueryResult(a_second_table, [all of a_second_tables columns])]
   */
  fun queryExposed(): List<QueryResult>

  data class QueryResult(val table: PsiNamedElement?, val columns: List<PsiElement>)
}