package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTableStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteInsertStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteLiteralValue
import com.alecstrong.sqlite.psi.core.psi.SqliteSetterExpression
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.alecstrong.sqlite.psi.core.psi.SqliteValuesExpression
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

internal abstract class LiteralValueMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteLiteralValue {
  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    if (node.findChildByType(setterOnlyLiterals) != null) {
      val values = PsiTreeUtil.getParentOfType(this, SqliteValuesExpression::class.java)

      if (values != null && values.parent is SqliteInsertStmt) return
      if (PsiTreeUtil.getParentOfType(this, SqliteSetterExpression::class.java) != null) return
      if (PsiTreeUtil.getParentOfType(this, SqliteCreateTableStmt::class.java) != null) return

      annotationHolder.createErrorAnnotation(this, "Cannot use time literal in expression")
    }
  }

  companion object {
    private val setterOnlyLiterals = TokenSet.create(
        SqliteTypes.CURRENT_DATE, SqliteTypes.CURRENT_TIME, SqliteTypes.CURRENT_TIMESTAMP)
  }
}