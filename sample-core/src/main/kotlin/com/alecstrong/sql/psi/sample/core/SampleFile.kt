package com.alecstrong.sql.psi.sample.core

import com.alecstrong.sql.psi.core.SqlFileBase
import com.intellij.psi.FileViewProvider

class SampleFile(viewProvider: FileViewProvider) : SqlFileBase(viewProvider, SampleLanguage, predefinedTables = emptyList()) {
  override val order = name.substringBefore(".${fileType.defaultExtension}").let { name ->
    if (name.all { it in '0'..'9' }) {
      name.toInt()
    } else {
      null
    }
  }
  override fun getFileType() = SampleFileType
  override fun toString() = "Sample File"
}
