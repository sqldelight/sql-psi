package com.alecstrong.sql.psi.core

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import com.alecstrong.sql.psi.test.fixtures.compileFiles
import org.junit.Test

class TablesExposedTest {
  @Test fun `tables works correctly for include all`() {
    compileFiles(
      """
      |CREATE TABLE test1 (
      |  id TEXT NOT NULL
      |);
      |
      |CREATE TABLE test2 (
      |  id TEXT NOT NULL
      |);
      |
      |CREATE TABLE test3 (
      |  id TEXT NOT NULL
      |);
      """.trimMargin(),
      """
      |CREATE TABLE test4 (
      |  id TEXT NOT NULL
      |);
      |
      |ALTER TABLE test2 ADD COLUMN id2 TEXT NOT NULL;
      |
      |ALTER TABLE test3 RENAME TO test5;
      """.trimMargin(),
      predefined = listOf(
        """
        |CREATE TABLE predefined (
        |  id TEXT NOT NULL
        |);
        """.trimMargin(),
      ),
    ) { (_, file) ->

      assertThat(file.tables(includeAll = true).map { it.tableName.text })
        .containsExactlyInAnyOrder(
          "predefined",
          "test1",
          "test2",
          "test4",
          "test5",
        )
    }
  }

  @Test fun `tables works correctly for include all=false`() {
    compileFiles(
      """
      |CREATE TABLE test1 (
      |  id TEXT NOT NULL
      |);
      |
      |CREATE TABLE test2 (
      |  id TEXT NOT NULL
      |);
      |
      |CREATE TABLE test3 (
      |  id TEXT NOT NULL
      |);
      """.trimMargin(),
      """
      |CREATE TABLE test4 (
      |  id TEXT NOT NULL
      |);
      |
      |ALTER TABLE test2 ADD COLUMN id2 TEXT NOT NULL;
      |
      |ALTER TABLE test3 RENAME TO test5;
      """.trimMargin(),
      predefined = listOf(
        """
        |CREATE TABLE predefined (
        |  id TEXT NOT NULL
        |);
        """.trimMargin(),
      ),
    ) { (_, file) ->

      assertThat(file.tables(includeAll = false).map { it.tableName.text })
        .containsExactlyInAnyOrder(
          "test2",
          "test4",
          "test5",
        )
    }
  }
}
