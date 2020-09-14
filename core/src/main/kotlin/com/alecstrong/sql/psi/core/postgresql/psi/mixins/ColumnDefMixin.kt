package com.alecstrong.sql.psi.core.postgresql.psi.mixins

import com.alecstrong.sql.psi.core.postgresql.psi.PostgreSqlTypeName
import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.alecstrong.sql.psi.core.psi.impl.SqlColumnDefImpl
import com.intellij.lang.ASTNode

internal class ColumnDefMixin(node: ASTNode) : SqlColumnDefImpl(node), SqlColumnDef {

  override fun hasDefaultValue(): Boolean {
    return isSerial() || super.hasDefaultValue()
  }
}

private fun SqlColumnDef.isSerial(): Boolean {
  val typeName = typeName as PostgreSqlTypeName
  return typeName.smallSerialDataType != null || typeName.serialDataType != null || typeName.bigSerialDataType != null
}
