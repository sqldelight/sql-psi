package com.alecstrong.sqlite.psi.sample.core

import com.alecstrong.sqlite.psi.core.SqliteFileBase
import com.intellij.psi.FileViewProvider

class SampleFile(viewProvider: FileViewProvider) : SqliteFileBase(viewProvider, SampleLanguage) {
  override fun getFileType() = SampleFileType
  override fun toString() = "Sample File"
}