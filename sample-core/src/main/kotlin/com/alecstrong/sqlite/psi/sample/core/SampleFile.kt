package com.alecstrong.sqlite.psi.sample.core

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class SampleFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, SampleLanguage) {
  override fun getFileType() = SampleFileType
  override fun toString() = "Sample File"
}