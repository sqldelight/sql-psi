package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil

internal abstract class CreateIndexMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateIndexStmt {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    if (PsiTreeUtil.getParentOfType(this, SqlStmtListMixin::class.java)!!.indexes().any { it != this }) {
      annotationHolder.createErrorAnnotation(indexName, "Duplicate index name ${indexName.text}")
    }
    super.annotate(annotationHolder)
  }
}