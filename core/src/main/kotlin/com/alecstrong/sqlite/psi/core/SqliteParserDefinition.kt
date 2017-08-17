package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements.MAY
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet

abstract class SqliteParserDefinition: ParserDefinition {
  private val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
  private val COMMENTS = TokenSet.create(SqliteTypes.COMMENT)

  init {
    SqliteElementType._language = fileNodeType.language
  }

  override fun createLexer(project: Project): Lexer = SqliteLexerAdapter()
  override fun createParser(project: Project): PsiParser = SqliteParser()
  override fun getWhitespaceTokens() = WHITE_SPACES
  override fun getCommentTokens() = COMMENTS
  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
  override fun spaceExistanceTypeBetweenTokens(p0: ASTNode, p1: ASTNode) = MAY
  override fun createElement(node: ASTNode): PsiElement = SqliteParserUtil.customSqliteParser.createElement(node)

  fun setParserOverride(customSqliteParser: CustomSqliteParser) {
    SqliteParserUtil.customSqliteParser = customSqliteParser
  }
}