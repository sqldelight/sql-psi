package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.SqlParser
import com.alecstrong.sql.psi.core.SqlParserDefinition
import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.GeneratedMarkerVisitor
import com.intellij.psi.impl.source.tree.TreeElement

internal abstract class SqlNamedElementImpl(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    PsiNameIdentifierOwner {
  abstract val parseRule: (builder: PsiBuilder, level: Int) -> Boolean

  override fun getName() = text

  override fun setName(name: String): PsiElement {
    // This whole thing is a hack. Its copied from an internal implementation of creating a fake
    // file tree, with some changes here so that we're not creating the entire fake file and just
    // doing the replacement inline. If this needs changing check out how Properties plugin
    // is implemented since it's inspired by that, which is documented online in IntelliJ's
    // official documentation for writing a language plugin. Good luck!

    val parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language) as SqlParserDefinition
    var builder = PsiBuilderFactory.getInstance().createBuilder(
        project, parent.node, parserDefinition.createLexer(project), language, name
    )
    builder = GeneratedParserUtilBase.adapt_builder_(node.elementType, builder,
        SqlParser(),
        SqlParser.EXTENDS_SETS_
    )
    GeneratedParserUtilBase.ErrorState.get(builder).currentFrame = GeneratedParserUtilBase.Frame()

    parseRule(builder, 0)
    val element = builder.treeBuilt
    (element as TreeElement).acceptTree(GeneratedMarkerVisitor())
    parent.node.replaceChild(node, element)
    return this
  }

  override fun getNameIdentifier(): PsiElement? {
    return findChildByType<PsiElement>(
        SqlTypes.ID)
  }
}
