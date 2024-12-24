package com.alecstrong.sql.psi.sample.headless

import com.alecstrong.sql.psi.core.psi.SqlLiteralExpr
import com.alecstrong.sql.psi.sample.core.psi.CustomExpr
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.io.File
import kotlin.reflect.KClass
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
              val literalExpr = columnDef.childrenOfType(SqlLiteralExpr::class).single()
              assertEquals(42, literalExpr.literalValue.numericLiteral!!.text.toInt())
            }
          }

          stmt.compoundSelectStmt != null -> {
            val select = stmt.compoundSelectStmt!!.selectStmtList.single()
            val exprs = select.exprList
            for (expr in exprs) {
              if (expr is CustomExpr) {
                val fooRule = expr.fooRule
                val literalExpr = fooRule.childrenOfType(SqlLiteralExpr::class).single().childrenOfType(SqlLiteralExpr::class).single()
                assertEquals(13, literalExpr.literalValue.numericLiteral!!.text.toInt())
              }
            }
          }
        }
      }
    }
  }
}

fun <T : PsiElement> PsiElement.childrenOfType(kClass: KClass<T>): List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, kClass.java)
