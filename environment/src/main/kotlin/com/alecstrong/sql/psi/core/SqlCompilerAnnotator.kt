package com.alecstrong.sql.psi.core

import com.intellij.psi.PsiElement

fun interface SqlCompilerAnnotator {
  fun annotate(element: PsiElement, annotationHolder: SqlAnnotationHolder)
}
