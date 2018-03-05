package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.AnnotationException
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.mixins.CreateVirtualTableMixin
import com.alecstrong.sqlite.psi.core.psi.mixins.SingleRow
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

internal class SqliteColumnReference<T: SqliteNamedElementImpl>(
    element: T
) : PsiReferenceBase<T>(element, TextRange.from(0, element.textLength)) {
  override fun handleElementRename(newElementName: String) = element.setName(newElementName)

  override fun resolve(): PsiElement? {
    return try {
      unsafeResolve()
    } catch (e: AnnotationException) {
      null
    }
  }

  internal fun unsafeResolve(): PsiElement? {
    if (element.parent is SqliteColumnDef || element.parent is CreateVirtualTableMixin) return element

    val tableName = tableName()
    val tables: List<QueryResult>
    if (tableName != null) {
      tables = availableQuery().filter { it.table?.name == tableName.name }

      if (tables.isEmpty()) {
        throw AnnotationException("No table found with name ${tableName.name}", tableName)
      }

    } else {
      tables = availableQuery().filterNot { it.table is SingleRow }
    }

    val columns = tables.flatMap { it.columns }
        .filterIsInstance<PsiNamedElement>()
        .filter { it.name == element.name }
    val synthesizedColumns = tables.flatMap { it.synthesizedColumns }
        .filter { element.name in it.acceptableValues }
        .map { it.table }
    val elements = columns + synthesizedColumns

    if (elements.size > 1) {
      throw AnnotationException("Multiple columns found with name ${element.name}")
    }
    return elements.firstOrNull()
  }

  override fun getVariants(): Array<Any> {
    tableName()?.let { tableName ->
      // Only include columns for the already specified table.
      return availableQuery().filter { it.table?.name == tableName.name }
          .flatMap { it.columns }
          .toLookupArray()
    }
    if (element.parent is SqliteColumnExpr) {
      // Also include table names.
      return availableQuery().flatMap { it.columns + it.table }.toLookupArray()
    }
    // Include all column names.
    return availableQuery().flatMap { it.columns }.toLookupArray()
  }

  private fun List<PsiElement?>.toLookupArray(): Array<Any> = filterIsInstance<PsiNamedElement>()
      .distinctBy { it.name }
      .map { LookupElementBuilder.create(it) }
      .toTypedArray()

  /**
   * Return the table that the column element this reference wraps belongs to, or null if no
   * table was specified.
   */
  private fun tableName(): PsiNamedElement? {
    val parent = element.parent
    if (parent is SqliteColumnExpr) {
      return parent.tableName
    }
    return null
  }

  private fun availableQuery(): List<QueryResult> {
    return (element.parent as SqliteCompositeElement).queryAvailable(element)
  }
}