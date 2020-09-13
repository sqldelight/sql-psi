package com.alecstrong.sql.psi.core.postgresql.psi.mixins

import com.alecstrong.sql.psi.core.postgresql.psi.PostgreSqlColumnDef
import com.alecstrong.sql.psi.core.postgresql.psi.PostgreSqlTypeName
import com.alecstrong.sql.psi.core.psi.impl.SqlColumnDefImpl
import com.intellij.lang.ASTNode

internal abstract class ColumnDefMixin(node: ASTNode) : SqlColumnDefImpl(node), PostgreSqlColumnDef {

  override fun hasDefaultValue(): Boolean {
    return columnConstraintList.any { isSerial() } || super.hasDefaultValue()
  }
}

private fun PostgreSqlColumnDef.isSerial(): Boolean {
  val typeName = typeName as PostgreSqlTypeName
  return typeName.smallSerialDataType != null || typeName.serialDataType != null || typeName.bigSerialDataType != null
}
