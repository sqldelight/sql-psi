package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.sqlite_3_18.psi.mixins.StatementValidatorMixin
import com.alecstrong.sql.psi.core.sqlite_3_18.SqliteParserUtil as Sqlite_3_18Util
import com.alecstrong.sql.psi.core.sqlite_3_18.psi.mixins.ColumnDefMixin as Sqlite_3_18ColumnDefMixin

enum class DialectPreset {
  SQLITE_3_18 {
    override fun setup() {
      SqlParserUtil.reset()
      Sqlite_3_18Util.reset()
      Sqlite_3_18Util.overrideSqlParser()

      val currentElementCreation = Sqlite_3_18Util.createElement
      Sqlite_3_18Util.createElement = {
        when (it.elementType) {
          SqlTypes.COLUMN_DEF -> Sqlite_3_18ColumnDefMixin(it)
          SqlTypes.STMT -> StatementValidatorMixin(it)
          else -> currentElementCreation(it)
        }
      }
    }
  };

  abstract fun setup()
}
