package com.alecstrong.sqlite.psi.core

import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.CoreProjectEnvironment
import com.intellij.lang.MetaLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.Disposer

class SqliteCoreEnvironment(
    parserDefinition: SqliteParserDefinition,
    fileType: LanguageFileType,
    disposable: Disposable = Disposer.newDisposable()
) {
  val applicationEnvironment = CoreApplicationEnvironment(disposable)
  val projectEnvironment = CoreProjectEnvironment(disposable, applicationEnvironment)

  init {
    CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), MetaLanguage.EP_NAME, MetaLanguage::class.java)

    with(applicationEnvironment) {
      registerFileType(fileType, fileType.defaultExtension)
      registerParserDefinition(parserDefinition)
    }
  }
}