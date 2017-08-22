package com.alecstrong.sqlite.psi.core.psi

import com.alecstrong.sqlite.psi.core.AnnotationException
import com.alecstrong.sqlite.psi.core.psi.SqliteQueryElement.QueryResult
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

internal class SqliteColumnReference<T: PsiNamedElement>(
    element: T
) : PsiReferenceBase<T>(element, TextRange.from(0, element.textLength)) {
  override fun resolve(): PsiElement? {
    return try {
      unsafeResolve()
    } catch (e: AnnotationException) {
      null
    }
  }

  internal fun unsafeResolve(): PsiElement? {
    if (element.parent is SqliteColumnDef) return element

    val elements: List<PsiNamedElement> = if (tableName() != null) {
      availableQuery().filter { it.table?.name == tableName() }
          .flatMap { it.columns }
          .filterIsInstance<PsiNamedElement>()
          .filter { it.name == element.name }
    } else {
      availableQuery().flatMap { it.columns }
          .filterIsInstance<PsiNamedElement>()
          .filter { it.name == element.name }
    }

    if (elements.size > 1) {
      throw AnnotationException("Multiple columns found with name ${element.name}")
    }
    return elements.firstOrNull()
  }

  override fun getVariants(): Array<Any> {
    tableName()?.let { tableName ->
      // Only include columns for the already specified table.
      return availableQuery().filter { it.table?.name == tableName }
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
  private fun tableName(): String? {
    val parent = element.parent
    if (parent is SqliteColumnExpr) {
      return parent.tableName?.name
    }
    if (parent is SqliteForeignKeyClause) {
      return parent.foreignTable.name
    }
    if (parent is SqliteIndexedColumn) {
      val indexedColumnParent = parent.parent
      if (indexedColumnParent is SqliteCreateIndexStmt) {
        return indexedColumnParent.tableName.name
      }
      if (indexedColumnParent is SqliteTableConstraint) {
        return (indexedColumnParent.parent as SqliteCreateTableStmt).tableName.name
      }
    }
    if (parent is SqliteTableConstraint) {
      return (parent.parent as SqliteCreateTableStmt).tableName.name
    }
    return null
  }

  private fun availableQuery(): List<QueryResult> {
    return (element.parent as SqliteCompositeElement).queryAvailable(element)
  }
}