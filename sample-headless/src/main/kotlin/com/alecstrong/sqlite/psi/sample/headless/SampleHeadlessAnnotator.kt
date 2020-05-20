package com.alecstrong.sql.psi.sample.headless

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.intellij.psi.PsiElement

internal class SampleHeadlessAnnotator : SqlAnnotationHolder {
  override fun createErrorAnnotation(element: PsiElement, s: String) {
    System.err.println(s)
  }
}
