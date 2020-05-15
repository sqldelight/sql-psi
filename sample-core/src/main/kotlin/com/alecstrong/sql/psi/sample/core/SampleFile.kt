package com.alecstrong.sql.psi.sample.core

import com.alecstrong.sql.psi.core.SqlFileBase
import com.intellij.psi.FileViewProvider

class SampleFile(viewProvider: FileViewProvider) : SqlFileBase(viewProvider, SampleLanguage) {
  override fun getFileType() = SampleFileType
  override fun toString() = "Sample File"
}
