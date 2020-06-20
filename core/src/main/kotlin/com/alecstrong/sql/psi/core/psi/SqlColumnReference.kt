package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.AnnotationException
import com.alecstrong.sql.psi.core.ModifiableFileLazy
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryColumn
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.mixins.CreateVirtualTableMixin
import com.alecstrong.sql.psi.core.psi.mixins.SingleRow
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

internal class SqlColumnReference<T : SqlNamedElementImpl>(
  element: T
) : PsiReferenceBase<T>(element, TextRange.from(0, element.textLength)) {
  override fun handleElementRename(newElementName: String) = element.setName(newElementName)

  private val resolved: QueryColumn? by ModifiableFileLazy(element.containingFile) {
    try {
      unsafeResolve()
    } catch (e: AnnotationException) {
      null
    }
  }

  override fun resolve() = resolved?.element

  internal fun resolveToQuery() = resolved

  internal fun unsafeResolve(): QueryColumn? {
    if (element.parent is SqlColumnDef || element.parent is CreateVirtualTableMixin) return QueryColumn(element)

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

    fun List<QueryResult>.matchingColumns(): List<QueryColumn> {
      val columns = flatMap { it.columns }
          .filter { it.element is PsiNamedElement && it.element.name == element.name }
      val synthesizedColumns = flatMap { it.synthesizedColumns }
          .filter { element.name in it.acceptableValues }
          .map { QueryColumn(it.table, it.nullable) }
      return columns + synthesizedColumns
    }

    val elements = tables.matchingColumns()

    if (elements.size > 1) {
      val adjacentColumns = tables.filter { it.adjacent }.matchingColumns()
      if (adjacentColumns.size != 1) {
        throw AnnotationException("Multiple columns found with name ${element.name}")
      }
      return adjacentColumns.firstOrNull()
    }
    return elements.firstOrNull()
  }

  override fun getVariants(): Array<Any> {
    tableName()?.let { tableName ->
      // Only include columns for the already specified table.
      return availableQuery().filter { it.table?.name == tableName.name }
          .flatMap { it.columns }
          .map { it.element }
          .toLookupArray()
    }
    if (element.parent is SqlColumnExpr) {
      // Also include table names.
      return availableQuery().flatMap { it.columns.map { it.element } + it.table }.toLookupArray()
    }
    // Include all column names.
    return availableQuery().flatMap { it.columns.map { it.element } }.toLookupArray()
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
    if (parent is SqlColumnExpr) {
      return parent.tableName
    }
    return null
  }

  private fun availableQuery(): Collection<QueryResult> {
    return (element.parent as SqlCompositeElement).queryAvailable(element)
  }
}
