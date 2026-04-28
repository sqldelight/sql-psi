package com.alecstrong.sql.psi.core.psi

/**
 * IndexElement represents a named index element implemented by CreateIndex, DropIndex, AlterIndex.
 * The indexElementName can be overridden to return the new name of an AlterIndex element
 */
interface IndexElement : SqlCompositeElement, SchemaContributor {
  fun indexElementName(): String = name()
}
