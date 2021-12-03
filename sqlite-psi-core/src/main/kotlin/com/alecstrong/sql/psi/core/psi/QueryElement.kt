package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.psi.QueryElement.QueryColumn
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface QueryElement : PsiElement {
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
  fun queryExposed(): Collection<QueryResult>

  /**
   * @param adjacent true if this query result comes from a FROM clause adjacent to the element
   *   asking for query results.
   */
  data class QueryResult(
    val table: PsiNamedElement? = null,
    val columns: List<QueryColumn>,
    val synthesizedColumns: List<SynthesizedColumn> = emptyList(),
    val joinConstraint: SqlJoinConstraint? = null,
    val adjacent: Boolean = false
  ) {
    constructor(column: PsiElement) : this(columns = listOf(QueryColumn(column)))

    override fun toString(): String {
      return "${table?.name} : [${columns.joinToString { it.element.text }}]"
    }
  }

  data class QueryColumn(
    val element: PsiElement,
    /**
     * If set, this overrides the nullability of the column. For example if you LEFT JOIN a table,
     * all its columns will be nullable, and if you check IS NOT NULL, it will not be nullable.
     */
    val nullable: Boolean? = null,
    val compounded: List<QueryColumn> = emptyList(),
    val hiddenByUsing: Boolean = false
  )

  /**
   * These aren't considered part of the exposed query (ie performing a SELECT * does not return
   * the column in the result set) but they can be explicitly referenced.
   */
  data class SynthesizedColumn(
    val table: PsiElement,
    val acceptableValues: List<String>,
    val nullable: Boolean = false
  )
}

internal fun List<PsiElement>.asColumns() = map { QueryColumn(it) }
