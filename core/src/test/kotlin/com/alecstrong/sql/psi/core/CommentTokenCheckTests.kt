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
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd))
      .isEqualTo("/** Documentation comment */")
  }

  @Test
  fun sqlComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "-- sql comment"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd))
      .isEqualTo("-- sql comment")
  }

  @Test
  fun blockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/* block comment */"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.BLOCK_COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd))
      .isEqualTo("/* block comment */")
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
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd))
      .isEqualTo("/* block comment */")
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
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd))
      .isEqualTo("/*vt+ SPECIFIC_FLAG*/")
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
  fun embeddedTrivialBlockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "SELECT /**/ * FROM foo /* z */"
    lexerAdapter.start(sql)
    lexerAdapter.advance()
    lexerAdapter.advance()
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

  @Test
  fun javadocWithExtraClosingStars() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/** doc **/"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.JAVADOC)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd))
      .isEqualTo("/** doc **/")
  }

  // Both regexes match all 5 characters; JAVADOC wins only because its rule is
  // declared first. Swap lines 203/204 of SqlLexer.flex and the suite stays green.
  @Test
  fun starsOnlyIsJavadoc() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/***/"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.JAVADOC)
  }

  @Test
  fun manyStarsIsBlockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "/**** x ****/"
    lexerAdapter.start(sql)
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.BLOCK_COMMENT)
    assertThat(sql.substring(lexerAdapter.tokenStart, lexerAdapter.tokenEnd))
      .isEqualTo("/**** x ****/")
  }

  // An unterminated block comment falls out to DIVIDE + MULTIPLY rather than
  // commenting to end-of-file. This is the IDE-typing case; pinning it means a
  // future change to that behaviour is deliberate.
  @Test
  fun unterminatedBlockComment() {
    val lexerAdapter = SqlLexerAdapter()
    val sql = "SELECT /* oops"
    lexerAdapter.start(sql)
    lexerAdapter.advance()
    lexerAdapter.advance()
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.DIVIDE)
    lexerAdapter.advance()
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.MULTIPLY)
    lexerAdapter.advance()
    lexerAdapter.advance()
    assertThat(lexerAdapter.tokenType).isEqualTo(SqlTypes.ID)
  }
}
