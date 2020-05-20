package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlInsertStmt
import com.alecstrong.sql.psi.core.psi.SqlLiteralValue
import com.alecstrong.sql.psi.core.psi.SqlSetterExpression
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.SqlValuesExpression
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

internal abstract class LiteralValueMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlLiteralValue {
  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    if (node.findChildByType(setterOnlyLiterals) != null) {
      val values = PsiTreeUtil.getParentOfType(this, SqlValuesExpression::class.java)

      if (values != null && values.parent is SqlInsertStmt) return
      if (PsiTreeUtil.getParentOfType(this, SqlSetterExpression::class.java) != null) return
      if (PsiTreeUtil.getParentOfType(this, SqlCreateTableStmt::class.java) != null) return

      annotationHolder.createErrorAnnotation(this, "Cannot use time literal in expression")
    }
  }

  companion object {
    private val setterOnlyLiterals = TokenSet.create(
        SqlTypes.CURRENT_DATE, SqlTypes.CURRENT_TIME, SqlTypes.CURRENT_TIMESTAMP)
  }
}
