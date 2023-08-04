package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.SqlColumnExpr
import com.alecstrong.sql.psi.core.psi.mixins.getColumnDefOrNull
import com.alecstrong.sql.psi.core.psi.mixins.isForeignKey
import com.alecstrong.sql.psi.test.fixtures.compileFile
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GetForeignKeyHelperTest {
  @Test
  fun columnConstraint() {
    val sqlFile = compileFile(
      """
                |CREATE TABLE foo (
                | id INT PRIMARY KEY
                |);
                |
                |CREATE TABLE bar (
                |a TEXT REFERENCES foo(id)
                |);
                |
                |SELECT a FROM bar;
      """.trimMargin(),
    )
    val select = sqlFile.sqlStmtList!!.stmtList.last()
    val a = (select.compoundSelectStmt!!.selectStmtList.single().resultColumnList.single().expr as SqlColumnExpr).columnName

    val columnDef = a.getColumnDefOrNull()
    assertThat(columnDef).isNotNull()
    assertThat(columnDef!!.isForeignKey()).isTrue()
  }

  @Test
  fun tableConstraint() {
    val sqlFile = compileFile(
      """
                |CREATE TABLE foo (
                | id INT PRIMARY KEY
                |);
                |
                |CREATE TABLE bar (
                |a INT,
                |FOREIGN KEY (a) REFERENCES foo(id)
                |);
                |
                |SELECT a FROM bar;
      """.trimMargin(),
    )
    val select = sqlFile.sqlStmtList!!.stmtList.last()
    val a = (select.compoundSelectStmt!!.selectStmtList.single().resultColumnList.single().expr as SqlColumnExpr).columnName

    val columnDef = a.getColumnDefOrNull()
    assertThat(columnDef).isNotNull()
    assertThat(columnDef!!.isForeignKey()).isTrue()
  }

  @Test
  fun alterTable() {
    compileFile(
      """
                |CREATE TABLE foo (
                | id INT PRIMARY KEY
                |);
                |
                |CREATE TABLE bar (
                |b TEXT
                |);
      """.trimMargin(),
      fileName = "1.s",
    )
    val alterFile = compileFile(
      """
                |ALTER TABLE bar
                | ADD COLUMN a INT REFERENCES foo(id)
                |;
                |
                |SELECT a FROM bar;
      """.trimMargin(),
      fileName = "2.s",
    )
    val select = alterFile.sqlStmtList!!.stmtList.last()
    val a = (select.compoundSelectStmt!!.selectStmtList.single().resultColumnList.single().expr as SqlColumnExpr).columnName

    val columnDef = a.getColumnDefOrNull()
    assertThat(columnDef).isNotNull()
    assertThat(columnDef!!.isForeignKey()).isTrue()
  }
}
