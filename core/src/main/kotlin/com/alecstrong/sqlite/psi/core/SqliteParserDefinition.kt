package com.alecstrong.sqlite.psi.core

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements.MAY
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import generated.GeneratedParser
import generated.GeneratedTypes

abstract class SqliteParserDefinition: ParserDefinition {
  private val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
  private val COMMENTS = TokenSet.create(GeneratedTypes.COMMENT)

  init {
    SqliteElementType._language = fileNodeType.language
  }

  override fun createLexer(project: Project): Lexer = SqliteLexerAdapter()
  override fun createParser(project: Project): PsiParser = GeneratedParser()
  override fun getWhitespaceTokens() = WHITE_SPACES
  override fun getCommentTokens() = COMMENTS
  override fun getStringLiteralElements() = TokenSet.EMPTY
  override fun spaceExistanceTypeBetweenTokens(p0: ASTNode?, p1: ASTNode?) = MAY
  override fun createElement(node: ASTNode) = GeneratedTypes.Factory.createElement(node)
}