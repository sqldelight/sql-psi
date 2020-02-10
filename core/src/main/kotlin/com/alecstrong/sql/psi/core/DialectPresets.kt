package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.sqlite_3_24.SqliteParserUtil as Sqlite_3_24Util

enum class DialectPreset {
  SQLITE, SQLITE_3_24;

  fun setup() {
    when (this) {
      SQLITE -> {
        SqlParserUtil.reset()
      }
      SQLITE_3_24 -> {
        SqlParserUtil.reset()
        Sqlite_3_24Util.reset()
        Sqlite_3_24Util.overrideSqlParser()
      }
    }
  }
}