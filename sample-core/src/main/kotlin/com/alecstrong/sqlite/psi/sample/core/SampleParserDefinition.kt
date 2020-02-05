package com.alecstrong.sqlite.psi.sample.core

import com.alecstrong.sqlite.psi.core.SqliteParserDefinition
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase.Parser
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType

class SampleParserDefinition : SqliteParserDefinition() {
  init {
    SampleSqliteParserUtil.overrideSqliteParser()
  }

  override fun createFile(fileViewProvider: FileViewProvider) = SampleFile(fileViewProvider)
  override fun getFileNodeType() = FILE
  override fun getLanguage() = SampleLanguage

  companion object {
    val FILE = IFileElementType(SampleLanguage)
  }
}