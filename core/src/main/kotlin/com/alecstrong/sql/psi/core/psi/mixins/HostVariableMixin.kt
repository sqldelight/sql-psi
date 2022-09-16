package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlParser
import com.alecstrong.sql.psi.core.psi.SqlHostVariableId
import com.alecstrong.sql.psi.core.psi.SqlNamedElementImpl
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder

internal abstract class HostVariableMixin(
  node: ASTNode,
) : SqlNamedElementImpl(node), SqlHostVariableId {
  override val parseRule: (builder: PsiBuilder, level: Int) -> Boolean = SqlParser::host_variable_id_real
}
