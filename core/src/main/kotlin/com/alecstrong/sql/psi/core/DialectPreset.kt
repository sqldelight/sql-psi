package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.hsql.HsqlParserUtil
import com.alecstrong.sql.psi.core.mysql.MySqlParserUtil
import com.alecstrong.sql.psi.core.postgresql.PostgreSqlParserUtil
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.sqlite_3_18.psi.mixins.StatementValidatorMixin
import com.alecstrong.sql.psi.core.mysql.psi.mixins.ColumnDefMixin as MySqlColumnDefMixin
import com.alecstrong.sql.psi.core.postgresql.psi.mixins.ColumnDefMixin as PostgreSqlColumnDefMixin
import com.alecstrong.sql.psi.core.sqlite_3_18.SqliteParserUtil as Sqlite_3_18Util
import com.alecstrong.sql.psi.core.sqlite_3_18.psi.mixins.ColumnDefMixin as Sqlite_3_18ColumnDefMixin
import com.alecstrong.sql.psi.core.sqlite_3_24.SqliteParserUtil as Sqlite_3_24Util
import com.alecstrong.sql.psi.core.sqlite_3_25.SqliteParserUtil as Sqlite_3_25Util

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
  },
  SQLITE_3_24 {
    override fun setup() {
      SQLITE_3_18.setup()
      Sqlite_3_24Util.reset()
      Sqlite_3_24Util.overrideSqlParser()
    }
  },
  SQLITE_3_25 {
    override fun setup() {
      SQLITE_3_24.setup()
      Sqlite_3_25Util.reset()
      Sqlite_3_25Util.overrideSqlParser()
    }
  },
  MYSQL {
    override fun setup() {
      SqlParserUtil.reset()
      MySqlParserUtil.reset()
      MySqlParserUtil.overrideSqlParser()

      val currentElementCreation = MySqlParserUtil.createElement
      MySqlParserUtil.createElement = {
        when (it.elementType) {
          SqlTypes.COLUMN_DEF -> MySqlColumnDefMixin(it)
          else -> currentElementCreation(it)
        }
      }
    }
  },
  POSTGRESQL {
    override fun setup() {
      SqlParserUtil.reset()
      PostgreSqlParserUtil.reset()
      PostgreSqlParserUtil.overrideSqlParser()

      val currentElementCreation = PostgreSqlParserUtil.createElement
      PostgreSqlParserUtil.createElement = {
        when (it.elementType) {
          SqlTypes.COLUMN_DEF -> PostgreSqlColumnDefMixin(it)
          else -> currentElementCreation(it)
        }
      }
    }
  },
  HSQL {
    override fun setup() {
      SqlParserUtil.reset()
      HsqlParserUtil.reset()
      HsqlParserUtil.overrideSqlParser()
    }
  };

  abstract fun setup()
}
