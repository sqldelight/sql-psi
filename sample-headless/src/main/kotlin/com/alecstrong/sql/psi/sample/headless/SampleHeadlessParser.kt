package com.alecstrong.sql.psi.sample.headless

import com.alecstrong.sql.psi.core.SqlCoreEnvironment
import com.alecstrong.sql.psi.sample.core.SampleFile
import com.alecstrong.sql.psi.sample.core.SampleFileType
import com.alecstrong.sql.psi.sample.core.SampleParserDefinition
import com.intellij.psi.PsiDocumentManager
import java.io.File
import java.nio.file.Path

class SampleHeadlessParser {
  fun parseSqlite(sourceFolders: List<Path>, onError: (String) -> Unit): List<SampleFile> {
    val parserDefinition = SampleParserDefinition()
    val environment = object : SqlCoreEnvironment(
      sourceFolders = sourceFolders,
      dependencies = emptyList(),
    ) {
      init {
        initializeApplication {
          registerFileType(SampleFileType, SampleFileType.defaultExtension)
          registerParserDefinition(parserDefinition)
        }
      }

      val project = projectEnvironment.project
    }
    environment.annotate { element, message ->
      val file = PsiDocumentManager.getInstance(environment.project).getDocument(element.containingFile)!!
      val error = "${element.containingFile.virtualFile.path}: (${file.getLineNumber(element.textOffset) + 1}, ${
        element.textOffset - file.getLineStartOffset(file.getLineNumber(element.textOffset))
      }): $message"
      onError(error)
    }
    return buildList {
      environment.forSourceFiles<SampleFile> {
        add(it)
      }
    }
  }
}

fun main() {
  SampleHeadlessParser().parseSqlite(listOf(File("sample-headless").toPath())) {
    System.err.println(it)
  }
}
