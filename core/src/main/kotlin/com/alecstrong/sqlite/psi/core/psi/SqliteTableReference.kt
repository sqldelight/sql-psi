package com.alecstrong.sqlite.psi.core.psi

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

internal class SqliteTableReference<T: SqliteNamedElementImpl>(
    element: T
) : PsiReferenceBase<T>(element, TextRange.from(0, element.textLength)) {
  override fun handleElementRename(newElementName: String) = element.setName(newElementName)

  override fun resolve(): PsiElement? {
    if (element.parent.isDefinition()) return element
    return variants.mapNotNull { it.psiElement }
        .filterIsInstance<PsiNamedElement>()
        .firstOrNull { it.name == element.name }
  }

  override fun getVariants(): Array<LookupElement> {
    if (element.parent.isDefinition()) return emptyArray()
    if (selectFromCurrentQuery()) {
      return (element.parent as SqliteCompositeElement).queryAvailable(element)
          .mapNotNull { it.table?.let(LookupElementBuilder::create) }
          .toTypedArray()
    }
    return (element.parent as SqliteCompositeElement).tablesAvailable(element)
        .map { LookupElementBuilder.create(it.tableName) }
        .toTypedArray()
  }

  private fun PsiElement.isDefinition() = when (this) {
    is SqliteCreateTableStmt -> true
    is SqliteCteTableName -> true
    is SqliteCreateVirtualTableStmt -> true
    is SqliteCreateViewStmt -> true
    else -> false
  }

  private fun selectFromCurrentQuery(): Boolean {
    return element.parent is SqliteColumnExpr || element.parent is SqliteResultColumn
  }
}