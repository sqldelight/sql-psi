package com.alecstrong.sqlite.psi.core

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType

internal class TestHeadlessParser {
  private val parserDefinition = TestParserDefinition()

  fun build(root: String, annotator: SqliteAnnotationHolder) {
    val environment = SqliteCoreEnvironment(parserDefinition, TestFileType, root)
    environment.annotate(annotator)
  }
}

private object TestLanguage : Language("Test")
private object TestFileType : LanguageFileType(TestLanguage) {
  override fun getIcon() = AllIcons.Icon
  override fun getName() = "Test File"
  override fun getDefaultExtension() = "s"
  override fun getDescription() = "Test SQLite Language File"
}

private class TestParserDefinition: SqliteParserDefinition() {
  override fun createFile(p0: FileViewProvider) = TestFile(p0)
  override fun getFileNodeType() = FILE

  companion object {
    val FILE = IFileElementType(TestLanguage)
  }
}

private class TestFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, TestLanguage) {
  override fun getFileType() = TestFileType
}