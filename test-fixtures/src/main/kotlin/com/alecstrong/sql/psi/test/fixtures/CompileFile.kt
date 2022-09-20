package com.alecstrong.sql.psi.test.fixtures

import com.alecstrong.sql.psi.core.PredefinedTable
import com.alecstrong.sql.psi.core.SqlFileBase
import java.io.File

fun compileFile(text: String, fileName: String = "temp.s", predefined: List<PredefinedTable> = emptyList()): SqlFileBase {
  val directory = File("build/tmp").apply { mkdirs() }
  val file = File(directory, fileName).apply {
    createNewFile()
    deleteOnExit()
  }
  file.writeText(text)

  val parser = TestHeadlessParser()
  val environment = parser.build(
    root = directory.path,
    annotator = { element, message ->
      throw AssertionError("at ${element.textOffset} : $message")
    },
    predefinedTables = predefined,
  )

  var result: SqlFileBase? = null
  environment.forSourceFiles<SqlFileBase> {
    if (it.name == fileName) result = it
  }
  return result!!
}
