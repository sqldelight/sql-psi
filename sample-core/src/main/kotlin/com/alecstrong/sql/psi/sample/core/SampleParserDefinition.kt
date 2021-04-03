package com.alecstrong.sql.psi.sample.core

import com.alecstrong.sql.psi.core.SqlParserDefinition
import com.intellij.psi.FileViewProvider
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.ILightStubFileElementType

class SampleParserDefinition : SqlParserDefinition() {
  init {
    SampleSqliteParserUtil.overrideSqlParser()
  }

  override fun createFile(viewProvider: FileViewProvider) = SampleFile(viewProvider)
  override fun getFileNodeType() = FILE
  override fun getLanguage() = SampleLanguage

  companion object {
    val FILE = ILightStubFileElementType<PsiFileStub<SampleFile>>(SampleLanguage)
  }
}
