package com.alecstrong.sql.psi.core

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.alecstrong.sql.psi.test.fixtures.compileFile
import org.junit.Test

class NullabilityTest {
  @Test
  fun outerJoin() {
    compileFile(
      """
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
      |FROM (SELECT owner.name, car.brand AS carBrand, car.rowid AS rowid
      |      FROM owner
      |      LEFT OUTER JOIN car ON owner.carId = car.id)
      |WHERE carBrand = ?;
      """
        .trimMargin()
    ) { file ->
      val select = file.sqlStmtList!!.stmtList.mapNotNull { it.compoundSelectStmt }.single()
      val projection = select.queryExposed().flatMap { it.columns }

      assertThat(projection[0].nullable).isNull()
      assertThat(projection[1].nullable).isNotNull().isTrue()
      assertThat(projection[2].nullable).isNotNull().isTrue()
    }
  }

  @Test
  fun leftJoinGroupBy() {
    compileFile(
      """
      |CREATE TABLE target (
      |  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
      |  coacheeId INTEGER NOT NULL,
      |  name TEXT NOT NULL
      |);
      |
      |CREATE TABLE challengeTarget (
      |  targetId INTEGER NOT NULL,
      |  challengeId INTEGER NOT NULL
      |);
      |
      |CREATE TABLE challenge (
      |  id INTEGER NOT NULL,
      |  cancelledAt INTEGER,
      |  emoji TEXT
      |);
      |
      |SELECT target.id AS id, target.name AS name, challenge.emoji
      |  FROM target
      |  LEFT JOIN challengeTarget
      |    ON challengeTarget.targetId = target.id
      |  LEFT JOIN challenge
      |    ON challengeTarget.challengeId = challenge.id AND challenge.cancelledAt IS NULL
      |  WHERE target.coacheeId = ?
      |  GROUP BY 1
      |  ORDER BY target.name COLLATE NOCASE ASC
      |;
      """
        .trimMargin()
    ) { file ->
      val select = file.sqlStmtList!!.stmtList.mapNotNull { it.compoundSelectStmt }.single()
      val projection = select.queryExposed().flatMap { it.columns }

      assertThat(projection[0].nullable).isNull()
      assertThat(projection[1].nullable).isNull()
      assertThat(projection[2].nullable).isNotNull().isTrue()
    }
  }

  @Test
  fun checkNotNull() {
    compileFile(
      """
      |CREATE TABLE target (
      |  name TEXT,
      |  thing TEXT
      |);
      |
      |SELECT name, name AS poop, thing AS poop2
      |FROM target
      |WHERE name IS NOT NULL AND thing IS NOT NULL;
      """
        .trimMargin()
    ) { file ->
      val select = file.sqlStmtList!!.stmtList.mapNotNull { it.compoundSelectStmt }.single()
      val projection = select.queryExposed().flatMap { it.columns }

      assertThat(projection[0].nullable).isNotNull().isFalse()
      assertThat(projection[1].nullable).isNotNull().isFalse()
      assertThat(projection[2].nullable).isNotNull().isFalse()
    }
  }
}
