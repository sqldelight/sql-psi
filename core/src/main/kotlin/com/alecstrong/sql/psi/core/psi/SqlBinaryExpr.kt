package com.alecstrong.sql.psi.core.psi

interface SqlBinaryExpr : SqlExpr {
  fun getExprList(): List<SqlExpr>
}