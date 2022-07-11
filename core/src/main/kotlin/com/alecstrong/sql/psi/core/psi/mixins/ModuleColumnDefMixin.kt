package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlModuleColumnDef
import com.intellij.lang.ASTNode

// Throws if you access typeName and one doesn't
// exist — typeName is optional for ModuleColumnDef
internal abstract class ModuleColumnDefMixin(
  node: ASTNode,
) : SqlCompositeElementImpl(node), SqlModuleColumnDef, SqlColumnDef
