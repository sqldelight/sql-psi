package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.SqliteParserDefinition
import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.GeneratedMarkerVisitor
import com.intellij.psi.impl.source.tree.TreeElement

internal abstract class SqliteNamedElementImpl(
  node: ASTNode
) : SqliteCompositeElementImpl(node),
    PsiNameIdentifierOwner {
  abstract val parseRule: (builder: PsiBuilder, level: Int) -> Boolean

  override fun getName(): String {
    val text = this.text
    return when {
      text.startsWith('[') && text.endsWith(']') ||
      text.startsWith('"') && text.endsWith('"') ||
      text.startsWith('`') && text.endsWith('`') ||
      text.startsWith('\'') && text.endsWith('\'') -> text.substring(1, text.length - 1)
      else -> text
    }
  }

  override fun setName(name: String): PsiElement {
    // This whole thing is a hack. Its copied from an internal implementation of creating a fake
    // file tree, with some changes here so that we're not creating the entire fake file and just
    // doing the replacement inline. If this needs changing check out how Properties plugin
    // is implemented since it's inspired by that, which is documented online in IntelliJ's
    // official documentation for writing a language plugin. Good luck!

    val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language) as SqliteParserDefinition
    var builder = PsiBuilderFactory.getInstance().createBuilder(
        project, parent.node, parserDefinition.createLexer(project), language, name
    )
    builder = GeneratedParserUtilBase.adapt_builder_(node.elementType, builder, SqliteParser(),
        SqliteParser.EXTENDS_SETS_
    )
    GeneratedParserUtilBase.ErrorState.get(builder).currentFrame = GeneratedParserUtilBase.Frame()

    parseRule(builder, 0)
    val element = builder.treeBuilt
    (element as TreeElement).acceptTree(GeneratedMarkerVisitor())
    parent.node.replaceChild(node, element)
    return this
  }

  override fun getNameIdentifier(): PsiElement? {
    return findChildByType<PsiElement>(SqliteTypes.ID)
  }
}