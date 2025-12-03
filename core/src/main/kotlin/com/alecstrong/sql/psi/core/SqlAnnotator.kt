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
        element.annotate { _, message ->
          holder.newAnnotation(HighlightSeverity.ERROR, message).create()
        }
      }
    } catch (_: InvalidElementDetectedException) {
      // We can avoid annotating entirely if we encounter an invalid element and wait for the next
      // pass.
    }
  }
}

class AnnotationException(val msg: String, val element: PsiElement? = null) :
  IllegalStateException(msg)

fun interface SqlAnnotationHolder {
  fun createErrorAnnotation(element: PsiElement, message: String)
}
