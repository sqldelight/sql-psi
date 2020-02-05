package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.sqlite_3_24.SqliteParserUtil as Sqlite_3_24Util

enum class DialectPreset {
  SQLITE, SQLITE_3_24;

  fun setup() {
    when (this) {
      SQLITE -> {
        SqliteParserUtil.reset()
      }
      SQLITE_3_24 -> {
        SqliteParserUtil.reset()
        Sqlite_3_24Util.reset()
        Sqlite_3_24Util.overrideSqliteParser()
      }
    }
  }
}