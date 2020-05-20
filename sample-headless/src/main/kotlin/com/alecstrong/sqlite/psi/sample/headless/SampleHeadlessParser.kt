package com.alecstrong.sql.psi.sample.headless

import com.alecstrong.sql.psi.core.SqlCoreEnvironment
import com.alecstrong.sql.psi.sample.core.SampleFileType
import com.alecstrong.sql.psi.sample.core.SampleParserDefinition
import java.io.File

class SampleHeadlessParser {
  fun parseSqlite() {
    val parserDefinition = SampleParserDefinition()
    val environment = SqlCoreEnvironment(parserDefinition, SampleFileType, listOf(
        File("sample-headless")))
    environment.annotate(SampleHeadlessAnnotator())
  }
}

fun main(args: Array<String>) {
  SampleHeadlessParser().parseSqlite()
}
