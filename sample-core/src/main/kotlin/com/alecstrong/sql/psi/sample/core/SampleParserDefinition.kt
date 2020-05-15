package com.alecstrong.sql.psi.sample.core

import com.alecstrong.sql.psi.core.SqlParserDefinition
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType

class SampleParserDefinition : SqlParserDefinition() {
  init {
    SampleSqliteParserUtil.overrideSqlParser()
  }

  override fun createFile(fileViewProvider: FileViewProvider) = SampleFile(fileViewProvider)
  override fun getFileNodeType() = FILE
  override fun getLanguage() = SampleLanguage

  companion object {
    val FILE = IFileElementType(SampleLanguage)
  }
}
