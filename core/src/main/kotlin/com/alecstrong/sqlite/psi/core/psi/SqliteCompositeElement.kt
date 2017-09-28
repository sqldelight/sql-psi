package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.SqliteFileBase
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.intellij.psi.PsiElement

internal interface SqliteCompositeElement : PsiElement {
  /**
   * Returns the result set accessible by a given child.
   *
   * The result set are any rows that have already been selected and the operation is running on.
   * For example:
   *
   *   CREATE TRIGGER some_trigger
   *   BEFORE INSERT OF some_table
   *   BEGIN;
   *     SELECT *
   *     FROM some_table
   *     WHERE new._id = some_table._id;
   *   END;
   *
   * In this situation, everything between BEGIN and END has access to the "new" row, which is
   * its own result set with columns. However, "new" is not a table that can be selected from:
   *
   *   CREATE TRIGGER some_trigger
   *   BEFORE INSERT OF some_table
   *   BEGIN;
   *     SELECT *
   *     FROM new -- invalid
   *     WHERE new._id = some_table._id;
   *   END;
   */
  fun queryAvailable(child: PsiElement): List<QueryResult>

  /**
   * Returns a list of the selectable tables for the given child.
   *
   * The available tables are contextual because of common table expressions:
   *
   * WITH some_table AS (...)
   * SELECT *
   * FROM some_table
   *
   */
  fun tablesAvailable(child: PsiElement): List<LazyQuery>

  /**
   * Called by the annotator to annotate this element with any errors or warnings.
   */
  fun annotate(annotationHolder: SqliteAnnotationHolder)

  override fun getContainingFile(): SqliteFileBase
}

data class LazyQuery(val tableName: NamedElement, val query: () -> QueryResult)
