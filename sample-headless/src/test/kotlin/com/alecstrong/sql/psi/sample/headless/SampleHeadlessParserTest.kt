package com.alecstrong.sql.psi.sample.headless

import org.junit.Assert
import org.junit.Test
import java.io.File

class SampleHeadlessParserTest {
  @Test
  fun parserIsSuccessful() {
    SampleHeadlessParser().parseSqlite(listOf(File("../sample-headless"))) {
      Assert.fail(it)
    }
  }
}
