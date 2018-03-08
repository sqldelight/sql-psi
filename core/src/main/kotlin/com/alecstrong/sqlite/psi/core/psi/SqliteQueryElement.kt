package com.alecstrong.sqlite.psi.core.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface QueryElement: PsiElement {
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

  data class QueryResult(
    val table: PsiNamedElement?,
    val columns: List<PsiElement>,
    val synthesizedColumns: List<SynthesizedColumn> = emptyList(),
    val joinOperator: SqliteJoinOperator? = null
  ) {
    override fun toString(): String {
      return "${table?.name} : [${columns.joinToString { it.text }}]"
    }
  }

  /**
   * These aren't considered part of the exposed query (ie performing a SELECT * does not return
   * the column in the result set) but they can be explicitly referenced.
   */
  data class SynthesizedColumn(
    val table: PsiElement,
    val acceptableValues: List<String>
  )
}