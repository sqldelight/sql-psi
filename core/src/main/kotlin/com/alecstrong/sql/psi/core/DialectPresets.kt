package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.hsql.HsqlParserUtil
import com.alecstrong.sql.psi.core.mysql.MySqlParserUtil
import com.alecstrong.sql.psi.core.postgresql.PostgreSqlParserUtil
import com.alecstrong.sql.psi.core.sqlite_3_18.SqliteParserUtil as Sqlite_3_18Util
import com.alecstrong.sql.psi.core.sqlite_3_24.SqliteParserUtil as Sqlite_3_24Util
import com.alecstrong.sql.psi.core.sqlite_3_25.SqliteParserUtil as Sqlite_3_25Util

enum class DialectPreset {
  SQLITE_3_18, SQLITE_3_24, SQLITE_3_25, MYSQL, POSTGRESQL, HSQL;

  fun setup() {
    val exhaustive = when (this) {
      SQLITE_3_18 -> {
        SqlParserUtil.reset()
        Sqlite_3_18Util.reset()
        Sqlite_3_18Util.overrideSqlParser()
      }
      SQLITE_3_24 -> {
        SQLITE_3_18.setup()
        Sqlite_3_24Util.reset()
        Sqlite_3_24Util.overrideSqlParser()
      }
      SQLITE_3_25 -> {
        SQLITE_3_24.setup()
        Sqlite_3_25Util.reset()
        Sqlite_3_25Util.overrideSqlParser()
      }
      MYSQL -> {
        SqlParserUtil.reset()
        MySqlParserUtil.reset()
        MySqlParserUtil.overrideSqlParser()
      }
      HSQL -> {
        SqlParserUtil.reset()
        HsqlParserUtil.reset()
        HsqlParserUtil.overrideSqlParser()
      }
      POSTGRESQL -> {
        SqlParserUtil.reset()
        PostgreSqlParserUtil.reset()
        PostgreSqlParserUtil.overrideSqlParser()
      }
    }
  }
}
