package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.InvalidElementDetectedException
import com.alecstrong.sql.psi.core.psi.SqlCompositeElement
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

open class SqlAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    try {
      if (element is SqlCompositeElement) {
        element.annotate(AnnotationHolderImplWrapper(holder))
      }
    } catch (_: InvalidElementDetectedException) {
      // We can avoid annotating entirely if we encounter an invalid element and wait for the next
      // pass.
    }
  }
}

class AnnotationException(val msg: String, val element: PsiElement? = null) : IllegalStateException(msg)

interface SqlAnnotationHolder {
  fun createErrorAnnotation(element: PsiElement, s: String)
}

private class AnnotationHolderImplWrapper(val holder: AnnotationHolder) : SqlAnnotationHolder {
  override fun createErrorAnnotation(element: PsiElement, s: String) {
    holder.newAnnotation(HighlightSeverity.ERROR, s).create()
  }
}
