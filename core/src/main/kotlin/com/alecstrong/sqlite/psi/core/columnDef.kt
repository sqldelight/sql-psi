package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.SqliteColumnDef
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes

fun SqliteColumnDef.hasDefaultValue(): Boolean {
  return columnConstraintList.any {
    it.node.findChildByType(SqliteTypes.DEFAULT) != null
        || it.node.findChildByType(SqliteTypes.AUTOINCREMENT) != null
  } || columnConstraintList.none {
    it.node.findChildByType(SqliteTypes.NOT) != null
  } || (
      // An INTEGER PRIMARY KEY is still considered to have a default value, even without specifying AUTOINCREMENT:
      // https://www.sqlite.org/autoinc.html
      // "On an INSERT, if the ROWID or INTEGER PRIMARY KEY column is not explicitly given a value, then it will be
      // filled automatically with an unused integer .. regardless of whether or not the AUTOINCREMENT keyword is used."
      this.typeName.text == "INTEGER" && this.columnConstraintList.any {
        it.node.findChildByType(SqliteTypes.PRIMARY) != null
      }
      )
}