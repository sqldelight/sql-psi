package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.intellij.psi.PsiElement

interface SqlAnnotatedElement : PsiElement {
  /** Called by the annotator to annotate this element with any errors or warnings. */
  fun annotate(annotationHolder: SqlAnnotationHolder)
}
