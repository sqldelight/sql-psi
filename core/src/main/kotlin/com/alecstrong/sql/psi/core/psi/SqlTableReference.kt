package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.ModifiableFileLazy
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

internal class SqlTableReference<T : SqlNamedElementImpl>(
  element: T
) : PsiReferenceBase<T>(element, TextRange.from(0, element.textLength)) {
  override fun handleElementRename(newElementName: String) = element.setName(newElementName)

  private val resolved: PsiElement? by ModifiableFileLazy(element.containingFile) lazy@{
    if (element is SqlNewTableName) return@lazy element
    if (element.parent.isDefinition()) return@lazy element
    return@lazy variants.mapNotNull { it.psiElement }
        .filterIsInstance<PsiNamedElement>()
        .firstOrNull { it.name == element.name }
  }

  override fun resolve() = resolved

  override fun getVariants(): Array<LookupElement> {
    if (element is SqlNewTableName) return emptyArray()
    if (element.parent.isDefinition()) return emptyArray()
    if (selectFromCurrentQuery()) {
      return (element.parent as SqlCompositeElement).queryAvailable(element)
          .mapNotNull { it.table?.let(LookupElementBuilder::create) }
          .toTypedArray()
    }
    return (element.parent as SqlCompositeElement).tablesAvailable(element)
        .map { LookupElementBuilder.create(it.tableName) }
        .toTypedArray()
  }

  private fun PsiElement.isDefinition() = when (this) {
    is SqlCreateTableStmt -> true
    is SqlCteTableName -> true
    is SqlCreateVirtualTableStmt -> true
    is SqlCreateViewStmt -> true
    else -> false
  }

  private fun selectFromCurrentQuery(): Boolean {
    return element.parent is SqlColumnExpr || element.parent is SqlResultColumn
  }
}
