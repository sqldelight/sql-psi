package com.alecstrong.sql.psi.core

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.alecstrong.sql.psi.core.psi.SqlTypes
import org.junit.Test

class CommentTokenCheckTests {
  @Test
  fun jdocComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/** Documentation comment */"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.JAVADOC)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd)).isEqualTo("/** Documentation comment */")
  }

  @Test
  fun sqlComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "-- sql comment"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd)).isEqualTo("-- sql comment")
  }

  @Test
  fun blockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/* block comment */"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.BLOCK_COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd)).isEqualTo("/* block comment */")
  }

  // Test how these comments are actually used.
  @Test
  fun inlineBlockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "SELECT /* block comment */ * FROM foo"
    lexerAdapter.start(sql)
    lexerAdapter.advance()
    lexerAdapter.advance()
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.BLOCK_COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd)).isEqualTo("/* block comment */")
  }

  // Test how these comments are actually used.
  @Test
  fun complexBlockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "SELECT /*vt+ SPECIFIC_FLAG*/ * FROM foo"
    lexerAdapter.start(sql)
    lexerAdapter.advance()
    lexerAdapter.advance()
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.BLOCK_COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd)).isEqualTo("/*vt+ SPECIFIC_FLAG*/")
  }

  @Test
  fun multiLineBlockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/* block comment\nThat will not shut up */"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.BLOCK_COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd))
      .isEqualTo("/* block comment\nThat will not shut up */")
  }

  @Test
  fun trivialBlockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/**/"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.BLOCK_COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd)).isEqualTo("/**/")
  }

  @Test
  fun divisionEdgeCase() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.DIVIDE)
  }

  @Test
  fun multEdgeCase() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "*"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.MULTIPLY)
  }
}
