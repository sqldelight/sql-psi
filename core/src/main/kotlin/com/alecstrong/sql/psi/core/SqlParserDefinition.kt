package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements.MAY
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet

abstract class SqlParserDefinition : ParserDefinition {
  private val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
  private val COMMENTS = TokenSet.create(SqlTypes.COMMENT)

  init {
    SqlElementType._language = getLanguage()
  }

  override fun createLexer(project: Project): Lexer = SqlLexerAdapter()
  override fun getWhitespaceTokens() = WHITE_SPACES
  override fun getCommentTokens() = COMMENTS
  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
  override fun spaceExistanceTypeBetweenTokens(p0: ASTNode, p1: ASTNode) = MAY
  override fun createElement(node: ASTNode): PsiElement = SqlParserUtil.createElement(node)

  override fun createParser(project: Project) = SqlParser()

  abstract override fun createFile(p0: FileViewProvider): SqlFileBase
  abstract fun getLanguage(): Language
}
