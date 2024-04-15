package com.alecstrong.sql.psi.test.fixtures

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlCoreEnvironment
import com.alecstrong.sql.psi.core.SqlFileBase
import com.alecstrong.sql.psi.core.SqlParserDefinition
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.tree.IFileElementType
import java.nio.file.Path

object TestHeadlessParser {
  fun build(
    root: Path,
    annotator: SqlAnnotationHolder,
    predefinedTables: List<String> = emptyList(),
    customInit: CoreApplicationEnvironment.() -> Unit = { },
  ): SqlCoreEnvironment {
    return build(listOf(root), annotator, predefinedTables, customInit)
  }

  fun build(
    sourceFolders: List<Path>,
    annotator: SqlAnnotationHolder,
    predefinedTables: List<String> = emptyList(),
    customInit: CoreApplicationEnvironment.() -> Unit = { },
  ): SqlCoreEnvironment {
    val environment = object : SqlCoreEnvironment(
      sourceFolders = sourceFolders,
      dependencies = emptyList(),
    ) {
      init {
        initializeApplication {
          registerFileType(TestFileType, TestFileType.defaultExtension)
          val parserDefinition = TestParserDefinition(
            lazy {
              val factory = PsiFileFactory.getInstance(projectEnvironment.project)
              predefinedTables.map {
                factory.createFileFromText(TestLanguage, it) as SqlFileBase
              }
            },
          )
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

private class TestParserDefinition(private val predefinedTables: Lazy<List<SqlFileBase>>) : SqlParserDefinition() {
  override fun createFile(viewProvider: FileViewProvider) = TestFile(viewProvider, predefinedTables)
  override fun getFileNodeType() = FILE
  override fun getLanguage() = TestLanguage

  companion object {
    val FILE = IFileElementType(TestLanguage)
  }
}

private class TestFile(viewProvider: FileViewProvider, private val predefinedTables: Lazy<List<SqlFileBase>>) : SqlFileBase(viewProvider, TestLanguage) {
  override fun getFileType() = TestFileType
  override val order = name.substringBefore(".${fileType.defaultExtension}").let { name ->
    if (name.all { it in '0'..'9' }) {
      name.toLong()
    } else {
      null
    }
  }

  override fun baseContributorFiles(): List<SqlFileBase> {
    val base = super.baseContributorFiles()
    return base + predefinedTables.value
  }
}
