package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlLiteralValue
import com.intellij.lang.ASTNode

internal abstract class LiteralValueMixin(node: ASTNode) :
  SqlCompositeElementImpl(node), SqlLiteralValue
