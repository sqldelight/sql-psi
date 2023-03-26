package com.alecstrong.sql.psi.sample.headless

import com.alecstrong.sql.psi.core.SqlCoreEnvironment
import com.alecstrong.sql.psi.sample.core.SampleFileType
import com.alecstrong.sql.psi.sample.core.SampleParserDefinition
import java.io.File

class SampleHeadlessParser {
  fun parseSqlite() {
    val parserDefinition = SampleParserDefinition()
    val environment = object : SqlCoreEnvironment(
      sourceFolders = listOf(File("sample-headless")),
      dependencies = emptyList(),
      predefinedTables = emptyList(),
      language = parserDefinition.getLanguage(),
    ) {
      init {
        initializeApplication {
          registerFileType(SampleFileType, SampleFileType.defaultExtension)
          registerParserDefinition(parserDefinition)
        }
      }
    }
    environment.annotate { _, message ->
      System.err.println(message)
    }
  }
}

fun main() {
  SampleHeadlessParser().parseSqlite()
}
