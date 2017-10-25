package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.intellij.psi.PsiElement

interface SqliteAnnotatedElement: PsiElement {
  /**
   * Called by the annotator to annotate this element with any errors or warnings.
   */
  fun annotate(annotationHolder: SqliteAnnotationHolder)
}