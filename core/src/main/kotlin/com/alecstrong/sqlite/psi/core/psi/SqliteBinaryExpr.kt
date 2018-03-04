package com.alecstrong.sqlite.psi.core.psi

interface SqliteBinaryExpr : SqliteExpr {
  fun getExprList(): List<SqliteExpr>
}