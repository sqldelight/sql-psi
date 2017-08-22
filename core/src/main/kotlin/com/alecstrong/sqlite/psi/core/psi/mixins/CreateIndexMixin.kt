package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateIndexStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

internal abstract class CreateIndexMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateIndexStmt {
  override fun queryAvailable(child: PsiElement): List<QueryResult> {
    if (child in indexedColumnList || child == expr) {
      return tablesAvailable(child).filter { it.table?.name == tableName.name }
    }
    return super.queryAvailable(child)
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    if (PsiTreeUtil.getParentOfType(this, SqlStmtListMixin::class.java)!!.indexes()
        .any { it != this && it.indexName.text == indexName.text }) {
      annotationHolder.createErrorAnnotation(indexName, "Duplicate index name ${indexName.text}")
    }
    super.annotate(annotationHolder)
  }
}