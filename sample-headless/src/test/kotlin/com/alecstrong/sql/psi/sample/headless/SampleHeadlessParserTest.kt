package com.alecstrong.sql.psi.sample.headless

import com.alecstrong.sql.psi.core.psi.SqlLiteralExpr
import com.alecstrong.sql.psi.sample.core.psi.CustomExpr
import com.intellij.psi.util.childrenOfType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SampleHeadlessParserTest {
  @Test
  fun parserIsSuccessful() {
    val files = SampleHeadlessParser().parseSqlite(listOf(File("../sample-headless"))) {
      fail(it)
    }
    for (file in files) {
      val stmts = file.sqlStmtList ?: continue
      for (stmt in stmts.stmtList) {
        when {
          stmt.createTableStmt != null -> {
            val createTableStmt = stmt.createTableStmt!!
            for (columnDef in createTableStmt.columnDefList) {
              val literalExpr = columnDef.childrenOfType<SqlLiteralExpr>().single()
              assertEquals(42, literalExpr.literalValue.numericLiteral!!.text.toInt())
            }
          }

          stmt.compoundSelectStmt != null -> {
            val select = stmt.compoundSelectStmt!!.selectStmtList.single()
            val exprs = select.exprList
            for (expr in exprs) {
              if (expr is CustomExpr) {
                val fooRule = expr.fooRule
                val literalExpr = fooRule.childrenOfType<SqlLiteralExpr>().single().childrenOfType<SqlLiteralExpr>().single()
                assertEquals(13, literalExpr.literalValue.numericLiteral!!.text.toInt())
              }
            }
          }
        }
      }
    }
  }
}
