package com.alecstrong.sqlite.psi.sample.core

import com.alecstrong.sqlite.psi.core.SqliteParserDefinition
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType

class SampleParserDefinition : SqliteParserDefinition() {
  override fun createFile(fileViewProvider: FileViewProvider) = SampleFile(fileViewProvider)
  override fun getFileNodeType() = FILE

  companion object {
    val FILE = IFileElementType(SampleLanguage)
  }
}