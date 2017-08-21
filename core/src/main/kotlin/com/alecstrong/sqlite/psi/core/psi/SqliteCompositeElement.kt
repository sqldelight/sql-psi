package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.psi.PsiElement

internal interface SqliteCompositeElement : PsiElement {
  /**
   * Returns a list of the result set selectable by a given child. For example, in the select
   * statement
   *
   *   WITH common_table AS (
   *     SELECT *
   *     FROM table1
   *   )
   *   SELECT test_table.some_column
   *   FROM table1 AS test_table
   *   WHERE some_column = ?;
   *
   * the tables available to the result column are ["test_table"]. The tables available to the
   * from clause are ["common_table", all database tables/views], and the tables available to the
   * expression are ["test_table"].
   */
  fun queryAvailable(child: PsiElement): List<QueryResult>
}