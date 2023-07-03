package com.alecstrong.sql.psi.test.fixtures

import com.alecstrong.sql.psi.core.PredefinedTable
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlCoreEnvironment
import com.alecstrong.sql.psi.core.SqlFileBase
import com.alecstrong.sql.psi.core.SqlParserDefinition
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType
import java.io.File

object TestHeadlessParser {
  fun build(
    root: String,
    annotator: SqlAnnotationHolder,
    predefinedTables: List<PredefinedTable> = emptyList(),
    customInit: CoreApplicationEnvironment.() -> Unit = { },
  ): SqlCoreEnvironment {
    return build(listOf(File(root)), annotator, predefinedTables, customInit)
  }

  fun build(
    sourceFolders: List<File>,
    annotator: SqlAnnotationHolder,
    predefinedTables: List<PredefinedTable> = emptyList(),
    customInit: CoreApplicationEnvironment.() -> Unit = { },
  ): SqlCoreEnvironment {
    val parserDefinition = TestParserDefinition(predefinedTables)

    val environment = object : SqlCoreEnvironment(
      sourceFolders = sourceFolders,
      dependencies = emptyList(),
    ) {
      init {
        initializeApplication {
          registerFileType(TestFileType, TestFileType.defaultExtension)
          registerParserDefinition(parserDefinition)
          customInit()
        }
      }
    }
    environment.annotate(annotationHolder = annotator)
    return environment
  }
}

private object TestLanguage : Language("Test")
private object TestFileType : LanguageFileType(TestLanguage) {
  override fun getIcon() = AllIcons.Icon
  override fun getName() = "Test File"
  override fun getDefaultExtension() = "s"
  override fun getDescription() = "Test SQLite Language File"
}

private class TestParserDefinition(private val predefinedTables: List<PredefinedTable>) : SqlParserDefinition() {
  override fun createFile(viewProvider: FileViewProvider) = TestFile(viewProvider, predefinedTables)
  override fun getFileNodeType() = FILE
  override fun getLanguage() = TestLanguage

  companion object {
    val FILE = IFileElementType(TestLanguage)
  }
}

private class TestFile(viewProvider: FileViewProvider, predefinedTables: List<PredefinedTable>) : SqlFileBase(viewProvider, TestLanguage, predefinedTables) {
  override fun getFileType() = TestFileType
  override val order = name.substringBefore(".${fileType.defaultExtension}").let { name ->
    if (name.all { it in '0'..'9' }) {
      name.toLong()
    } else {
      null
    }
  }
}
