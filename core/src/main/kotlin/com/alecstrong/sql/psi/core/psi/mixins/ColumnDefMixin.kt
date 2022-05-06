package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.lang.ASTNode

abstract class ColumnDefMixin(node: ASTNode) : SqlCompositeElementImpl(node), SqlColumnDef {

  open fun hasDefaultValue(): Boolean {
    return columnConstraintList.any { it.defaultConstraint != null } ||
      columnConstraintList.none { it.node.findChildByType(SqlTypes.NOT) != null } ||
      columnConstraintList.any { it.generatedClause != null }
  }
}
