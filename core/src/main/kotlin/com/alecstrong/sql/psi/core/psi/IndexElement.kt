package com.alecstrong.sql.psi.core.psi

import com.intellij.psi.StubBasedPsiElement

/**
 * IndexElement represents a named index element implemented by CreateIndex, DropIndex, AlterIndex.
 * The indexElementName can be overridden to return the new name of an AlterIndex element
 */
interface IndexElement :
  SqlCompositeElement, SchemaContributor, StubBasedPsiElement<SchemaContributorStub> {
  fun indexElementName(): String = name()
}
