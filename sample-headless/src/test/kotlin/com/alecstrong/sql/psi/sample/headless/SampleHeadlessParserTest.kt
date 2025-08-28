package com.alecstrong.sql.psi.sample.headless

import SqliteTestFixtures
import com.alecstrong.sql.psi.core.psi.SqlLiteralExpr
import com.alecstrong.sql.psi.sample.core.SampleFile
import com.alecstrong.sql.psi.sample.core.psi.CustomExpr
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import kotlin.io.path.Path
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SampleHeadlessParserTest {
  @Test
  fun parserIsSuccessfulWithSourceFolder() {
    val files = SampleHeadlessParser().parseSqlite(listOf(Path("../sample-headless"))) {
      fail(it)
    }
    files.test()
  }

  @Test
  fun parserIsSuccessfulWithFileInJarSource() {
    val sqliteTestFixtures = SqliteTestFixtures()
    try {
      val test by SqliteTestFixtures()
      val files = SampleHeadlessParser().parseSqlite(listOf(test)) {
        fail(it)
      }
      files.test()
    } finally {
        sqliteTestFixtures.close()
    }
  }

  @Test
  fun parserIsSuccessfulWithSourceFolderAndFileInJarSource() {
    val sqliteTestFixtures = SqliteTestFixtures()
    val test2 by sqliteTestFixtures
    try {
      val files = SampleHeadlessParser().parseSqlite(listOf(Path("../sample-headless"), test2)) {
        fail(it)
      }
      files.test()
    } finally {
    sqliteTestFixtures.close()
  }
  }

  @Test
  fun parserIsSuccessfulWithJarSource() {
    val sqliteTestFixtures = SqliteTestFixtures()
    try {
      val files = SampleHeadlessParser().parseSqlite(listOf(sqliteTestFixtures.jarFile)) {
        fail(it)
      }
      assertEquals(2, files.size)
      files.test()
    } finally {
      sqliteTestFixtures.close()
    }
  }

  private fun List<SampleFile>.test() {
    for (file in this) {
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
