package com.alecstrong.sql.psi.core.sqlite_3_18.psi.mixins

import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.impl.SqlColumnDefImpl
import com.alecstrong.sql.psi.core.sqlite_3_18.psi.SqliteTypeName
import com.intellij.lang.ASTNode

internal class ColumnDefMixin(node: ASTNode) : SqlColumnDefImpl(node) {

  override fun hasDefaultValue(): Boolean {
    return columnConstraintList.any { it.node.findChildByType(SqlTypes.AUTOINCREMENT) != null } ||
      (
      // An INTEGER PRIMARY KEY is still considered to have a default value, even without specifying AUTOINCREMENT:
      // https://www.sqlite.org/autoinc.html
      // "On an INSERT, if the ROWID or INTEGER PRIMARY KEY column is not explicitly given a value, then it will be
      // filled automatically with an unused integer .. regardless of whether or not the AUTOINCREMENT keyword is used."
      (columnType.typeName as SqliteTypeName).intDataType != null &&
        this.columnConstraintList.any { it.node.findChildByType(SqlTypes.PRIMARY) != null }
      ) ||
      super.hasDefaultValue()
  }
}
