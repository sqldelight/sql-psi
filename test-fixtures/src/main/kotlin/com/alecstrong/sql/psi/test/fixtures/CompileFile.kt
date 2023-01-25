package com.alecstrong.sql.psi.test.fixtures

import com.alecstrong.sql.psi.core.SqlFileBase
import java.io.File

fun compileFile(text: String, fileName: String = "temp.s"): SqlFileBase {
  val directory = File("build/tmp").apply { mkdirs() }
  val file = File(directory, fileName).apply {
    createNewFile()
    deleteOnExit()
  }
  file.writeText(text)

  val parser = TestHeadlessParser()
  val environment = parser.build(
    root = directory.path,
  ) { element, message ->
    throw AssertionError("at ${element.textOffset} : $message")
  }

  var result: SqlFileBase? = null
  environment.forSourceFiles<SqlFileBase> {
    if (it.name == fileName) result = it
  }
  return result!!
}
