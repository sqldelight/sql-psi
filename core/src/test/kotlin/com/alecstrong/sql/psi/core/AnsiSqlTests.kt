package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.test.fixtures.FixturesTest
import java.io.File
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class AnsiSqlTests(name: String, fixtureRoot: File) : FixturesTest(name, fixtureRoot) {
  override val replaceRules =
    arrayOf(
      "?1" to "?",
      "?2" to "?",
      ":searchText" to "?",
      ":bind" to "?",
      ":bufferId" to "?",
      ":ignored" to "?",
      "AUTOINCREMENT" to "DEFAULT 0",
    )

  override fun setupDialect() {
    // No-op.
  }

  companion object {
    @Suppress("unused")
    // Used by Parameterized JUnit runner reflectively.
    @Parameters(name = "{0}")
    @JvmStatic
    fun parameters() = ansiFixtures
  }
}
