package com.alecstrong.sql.psi.core.sqlite_3_18.psi.mixins

import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.sqlite_3_18.psi.impl.SqliteAlterTableStmtImpl

internal class AlterTableElementType(
  name: String
) : SqlSchemaContributorElementType<TableElement>("sqlite_3_18.$name", TableElement::class.java) {
  override fun nameType() = SqlTypes.TABLE_NAME
  override fun createPsi(stub: SchemaContributorStub) = SqliteAlterTableStmtImpl(stub, this)
}
