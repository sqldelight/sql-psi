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
                |a TEXT,
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
}
