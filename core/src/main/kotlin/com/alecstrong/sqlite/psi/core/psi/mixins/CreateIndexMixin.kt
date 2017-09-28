package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

internal abstract class CreateIndexMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateIndexStmt {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child in indexedColumnList || child == expr) {
      return listOf(tablesAvailable(child).first { it.tableName.name == tableName.name }.query())
    }
    return super.queryAvailable(child)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    if (containingFile.indexes().any { it != this && it.indexName.text == indexName.text }) {
      annotationHolder.createErrorAnnotation(indexName, "Duplicate index name ${indexName.text}")
    }
    super.annotate(annotationHolder)
  }
}