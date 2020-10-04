package com.alecstrong.sql.psi.core.sqlite_3_18.psi.mixins

import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.mixins.AlterTableElementType
import com.alecstrong.sql.psi.core.psi.mixins.AlterTableStmtStub
import com.alecstrong.sql.psi.core.sqlite_3_18.psi.impl.SqliteAlterTableStmtImpl

internal class AlterTableElementType(
  name: String
) : AlterTableElementType("sqlite_3_18.$name") {
  override fun createPsi(stub: SchemaContributorStub) = SqliteAlterTableStmtImpl(
      stub as AlterTableStmtStub, this)
}
