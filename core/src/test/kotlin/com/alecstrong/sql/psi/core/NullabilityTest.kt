package com.alecstrong.sql.psi.core

import com.google.common.truth.Truth.assertThat
import com.intellij.psi.PsiElement
import java.io.File
import java.lang.AssertionError
import org.junit.Test

class NullabilityTest {
  @Test fun outerJoin() {
    val file = compileFile("""
      |CREATE TABLE car (
      |  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      |  id TEXT NOT NULL,
      |  brand TEXT NOT NULL
      |);
      |
      |CREATE TABLE owner (
      |  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      |  name TEXT NOT NULL,
      |  carId TEXT
      |);
      |
      |SELECT *
      |FROM (SELECT owner.name, car.brand AS carBrand
      |      FROM owner
      |      LEFT OUTER JOIN car ON owner.carId = car.id)
      |WHERE carBrand = ?;
    """.trimMargin())

    val select = file.sqlStmtList!!.stmtList.mapNotNull { it.compoundSelectStmt }.single()
    val projection = select.queryExposed().flatMap { it.columns }

    assertThat(projection[0].nullable).isFalse()
    assertThat(projection[1].nullable).isTrue()
  }

  private fun compileFile(text: String): SqlFileBase {
    val directory = File("build/tmp").apply { mkdirs() }
    val file = File(directory, "temp.s").apply {
      createNewFile()
      deleteOnExit()
    }
    file.writeText(text)

    val parser = TestHeadlessParser()
    val environment = parser.build(directory.path, object : SqlAnnotationHolder {
      override fun createErrorAnnotation(element: PsiElement, s: String) {
        throw AssertionError(s)
      }
    })

    var result: SqlFileBase? = null
    environment.forSourceFiles {
      result = (it as SqlFileBase)
    }
    return result!!
  }
}
