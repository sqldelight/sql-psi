package com.alecstrong.sql.psi.sample.headless

import com.alecstrong.sql.psi.core.SqlCoreEnvironment
import com.alecstrong.sql.psi.sample.core.SampleFileType
import com.alecstrong.sql.psi.sample.core.SampleParserDefinition
import java.io.File

class SampleHeadlessParser {
  fun parseSqlite() {
    val parserDefinition = SampleParserDefinition()
    val environment = object : SqlCoreEnvironment(listOf(File("sample-headless")), emptyList()) {
      init {
        initializeApplication {
          registerFileType(SampleFileType, SampleFileType.defaultExtension)
          registerParserDefinition(parserDefinition)
        }
      }
    }
    environment.annotate(SampleHeadlessAnnotator())
  }
}

fun main(args: Array<String>) {
  SampleHeadlessParser().parseSqlite()
}
