package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElement
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

open class SqliteAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is SqliteCompositeElement) {
      element.annotate(AnnotationHolderImplWrapper(holder))
    }
  }
}

class AnnotationException(msg: String) : IllegalStateException(msg)

interface SqliteAnnotationHolder {
  fun createErrorAnnotation(element: PsiElement, s: String?)
}

private class AnnotationHolderImplWrapper(val holder: AnnotationHolder) : SqliteAnnotationHolder {
  override fun createErrorAnnotation(element: PsiElement, s: String?) {
    holder.createErrorAnnotation(element, s)
  }
}