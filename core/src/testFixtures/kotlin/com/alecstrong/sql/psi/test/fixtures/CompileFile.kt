package com.alecstrong.sql.psi.test.fixtures

import com.alecstrong.sql.psi.core.SqlFileBase
import com.intellij.core.CoreApplicationEnvironment
import java.nio.file.Files
import kotlin.io.path.div
import kotlin.io.path.writeText

fun compileFile(
  // language=sql
  text: String,
  customInit: CoreApplicationEnvironment.() -> Unit = { },
  predefined: List<String> = emptyList(),
  action: (SqlFileBase) -> Unit,
) {
  compileFiles(text, predefined = predefined, customInit = customInit) {
    action(it.single())
  }
}

fun compileFiles(
  vararg files: String,
  customInit: CoreApplicationEnvironment.() -> Unit = { },
  predefined: List<String> = emptyList(),
  action: (List<SqlFileBase>) -> Unit,
) {
  val directory = Files.createTempDirectory("sql-psi")
  for ((index, content) in files.withIndex()) {
    val file = directory / "$index.s"
    file.writeText(content)
  }

  val environment = TestHeadlessParser.build(
    sourceFolders = listOf(directory),
    customInit = customInit,
    annotator = { element, message ->
      val tree = buildString {
        appendLine(element.containingFile.name)
        appendLine(element.containingFile.text)
        element.containingFile.printTree {
          append("  ")
          append(it)
        }
      }
      throw AssertionError("at ${element.textOffset} : $message\n$tree")
    },
    predefinedTables = predefined,
  )

  val sqlFilesMap = buildMap {
    environment.forSourceFiles<SqlFileBase> { sqlFile ->
      val index = sqlFile.name.removeSuffix(".s").toInt()
      put(index, sqlFile)
    }
  }
  val sqlFiles = List(sqlFilesMap.size) {
    sqlFilesMap[it]!!
  }
  action(sqlFiles)
  environment.close()
}
