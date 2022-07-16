package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.SqlBindParameter
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.intellij.lang.ASTNode

abstract class BindParameterMixin(node: ASTNode) : SqlCompositeElementImpl(node), SqlBindParameter {
  /**
   * Overwrite, if the user provided sql parameter should be overwritten by code generators with [replaceWith].
   *
   * Some sql dialects support other bind parameter besides `?`, but the code generator should still replace the
   * user provided parameter with [replaceWith] for a homogen generated code.
   */
  open val replaceWith: String = text

  /**
   * If true, this parameter is not used by code generators during parameter mapping.
   *
   * Some sql dialects support generated and default columns, so a code generator should skip this parameter to not
   * require and map it from the given parameters.
   */
  open val isDefault: Boolean = false
}
