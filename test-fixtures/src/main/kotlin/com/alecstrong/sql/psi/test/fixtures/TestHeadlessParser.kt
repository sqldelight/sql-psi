package com.alecstrong.sql.psi.test.fixtures

import com.alecstrong.sql.psi.core.PredefinedTable
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlCoreEnvironment
import com.alecstrong.sql.psi.core.SqlFileBase
import com.alecstrong.sql.psi.core.SqlParserDefinition
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType
import java.io.File

object TestHeadlessParser {
  fun build(root: String, annotator: SqlAnnotationHolder, predefinedTables: List<PredefinedTable>): SqlCoreEnvironment {
    val parserDefinition = TestParserDefinition(predefinedTables)

    val environment = object : SqlCoreEnvironment(
      sourceFolders = listOf(File(root)),
      dependencies = emptyList(),
    ) {
      init {
        // We need to update the new parser definition to get the new system tables.
        // Otherwise, the old parser is used without the updated content.
        updateApplication {
          LanguageParserDefinitions.INSTANCE.removeExplicitExtension(parserDefinition.getLanguage(), LanguageParserDefinitions.INSTANCE.forLanguage(parserDefinition.getLanguage()))
          registerParserDefinition(parserDefinition)
        }
        initializeApplication {
          registerFileType(TestFileType, TestFileType.defaultExtension)
          registerParserDefinition(parserDefinition)
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
    if (name.all { it in '0'..'9' }) name.toInt()
    else null
  }
}
