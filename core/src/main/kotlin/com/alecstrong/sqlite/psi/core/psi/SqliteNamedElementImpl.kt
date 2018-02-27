package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.SqliteParserDefinition
import com.alecstrong.sqlite.psi.core.parser.SqliteParser
import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.GeneratedMarkerVisitor
import com.intellij.psi.impl.source.tree.TreeElement

internal abstract class SqliteNamedElementImpl(
  node: ASTNode
) : SqliteCompositeElementImpl(node),
    PsiNamedElement {
  abstract val parseRule: (builder: PsiBuilder, level: Int) -> Boolean

  override fun getName() = text

  override fun setName(name: String): PsiElement {
    val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language) as SqliteParserDefinition
    var builder = PsiBuilderFactory.getInstance().createBuilder(
        project, parent.node, parserDefinition.createLexer(project), language, name
    )
    builder = GeneratedParserUtilBase.adapt_builder_(node.elementType, builder, SqliteParser(),
        SqliteParser.EXTENDS_SETS_
    )

    parseRule(builder, 0)
    val element = builder.treeBuilt
    (element as TreeElement).acceptTree(GeneratedMarkerVisitor())
    parent.node.replaceChild(node, element)
    return this
  }
}