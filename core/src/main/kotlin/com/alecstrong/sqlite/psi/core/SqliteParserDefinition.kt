package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements.MAY
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

abstract class SqliteParserDefinition: ParserDefinition {
  private val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
  private val COMMENTS = TokenSet.create(SqliteTypes.COMMENT)

  private var parserOverride: CustomSqliteParser = CustomSqliteParser()

  init {
    SqliteElementType._language = getLanguage()
  }

  override fun createLexer(project: Project): Lexer = SqliteLexerAdapter()
  override fun getWhitespaceTokens() = WHITE_SPACES
  override fun getCommentTokens() = COMMENTS
  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
  override fun spaceExistanceTypeBetweenTokens(p0: ASTNode, p1: ASTNode) = MAY
  override fun createElement(node: ASTNode): PsiElement = parserOverride.createElement(node)

  override fun createParser(project: Project): PsiParser {
    return object : SqliteParser() {
      override fun parse(
        root_: IElementType,
        builder_: PsiBuilder
      ): ASTNode {
        synchronized(SqliteParserUtil) {
          SqliteParserUtil.customSqliteParser = parserOverride
          return super.parse(root_, builder_)
        }
      }
    }
  }

  abstract override fun createFile(p0: FileViewProvider): SqliteFileBase
  abstract fun getLanguage(): Language

  fun setParserOverride(customSqliteParser: CustomSqliteParser) {
    parserOverride = customSqliteParser
  }
}