package com.alecstrong.sqlite.psi.sample.headless

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.intellij.psi.PsiElement

internal class SampleHeadlessAnnotator : SqliteAnnotationHolder {
  override fun createErrorAnnotation(element: PsiElement, s: String?) {
    System.err.println(s)
  }
}