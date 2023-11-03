package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.test.fixtures.TestHeadlessParser
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.nio.file.Files

class PassingPredefinedTablesTest {
  @Test
  fun mirrorSqlDelight() {
    val temp = Files.createTempDirectory("predefinedTest").toFile()
    File(temp, "Test.s").writeText(
      """
      SELECT * FROM dual;
      SELECT name FROM dual;
      """.trimIndent(),
    )
    val env = TestHeadlessParser.build(
      sourceFolders = listOf(temp),
      annotator = { _, message ->
        fail(message)
      },
      predefinedTables = listOf(
        """
        CREATE TABLE dual ( name TEXT );
        """.trimIndent(),
      ),
    )
    env.close()
  }
}
