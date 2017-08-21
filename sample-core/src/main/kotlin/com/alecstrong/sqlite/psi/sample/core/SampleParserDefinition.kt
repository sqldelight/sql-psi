package com.alecstrong.sqlite.psi.sample.core

import com.alecstrong.sqlite.psi.core.CustomSqliteParser
import com.alecstrong.sqlite.psi.core.SqliteParserDefinition
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase.Parser
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType

class SampleParserDefinition : SqliteParserDefinition() {
  init {
    setParserOverride(object : CustomSqliteParser() {
      override fun columnDef(builder: PsiBuilder, level: Int, column_def: Parser): Boolean {
        return SampleParser.column_def(builder, level)
      }

      override fun createElement(node: ASTNode): PsiElement {
        return try {
          SampleTypes.Factory.createElement(node)
        } catch (e: AssertionError) {
          super.createElement(node)
        }
      }
    })
  }

  override fun createFile(fileViewProvider: FileViewProvider) = SampleFile(fileViewProvider)
  override fun getFileNodeType() = FILE

  companion object {
    val FILE = IFileElementType(SampleLanguage)
  }
}