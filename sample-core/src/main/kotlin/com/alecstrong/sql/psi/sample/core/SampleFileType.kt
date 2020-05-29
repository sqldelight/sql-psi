package com.alecstrong.sql.psi.sample.core

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType

object SampleFileType : LanguageFileType(SampleLanguage) {
  override fun getIcon() = AllIcons.Debugger.Db_db_object
  override fun getName() = "Sample File"
  override fun getDefaultExtension() = "samplesql"
  override fun getDescription() = "Sample SQLite Language File"
}
