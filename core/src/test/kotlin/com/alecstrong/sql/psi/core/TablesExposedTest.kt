package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.test.fixtures.compileFile
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class TablesExposedTest {
  @Before
  fun before() {
    File("build/tmp").deleteRecursively()
  }

  @After
  fun after() {
    SqlParserUtil.reset()
    File("build/tmp").deleteRecursively()
  }

  @Test fun `tables works correctly for include all`() {
    compileFile(
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
      "1.s",
      predefined = listOf(
        PredefinedTable(
          "predefined",
          "1.predefined",
          """
      |CREATE TABLE predefined (
      |  id TEXT NOT NULL
      |);
          """.trimMargin(),
        ),
      ),
    )
    val file = compileFile(
      """
      |CREATE TABLE test4 (
      |  id TEXT NOT NULL
      |);
      |
      |ALTER TABLE test2 ADD COLUMN id2 TEXT NOT NULL;
      |
      |ALTER TABLE test3 RENAME TO test5;
      """.trimMargin(),
      "2.s",
    )

    assertThat(file.tables(includeAll = true).map { it.tableName.text }).containsExactly(
      "test1",
      "test2",
      "test4",
      "test5",
    )
  }

  @Test fun `tables works correctly for include all=false`() {
    compileFile(
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
      "1.s",
      predefined = listOf(
        PredefinedTable(
          "predefined",
          "1.predefined",
          """
      |CREATE TABLE predefined (
      |  id TEXT NOT NULL
      |);
          """.trimMargin(),
        ),
      ),
    )
    val file = compileFile(
      """
      |CREATE TABLE test4 (
      |  id TEXT NOT NULL
      |);
      |
      |ALTER TABLE test2 ADD COLUMN id2 TEXT NOT NULL;
      |
      |ALTER TABLE test3 RENAME TO test5;
      """.trimMargin(),
      "2.s",
    )

    assertThat(file.tables(includeAll = false).map { it.tableName.text }).containsExactly(
      "test2",
      "test4",
      "test5",
    )
  }
}
