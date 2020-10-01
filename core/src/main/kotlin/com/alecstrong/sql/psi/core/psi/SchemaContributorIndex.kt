package com.alecstrong.sql.psi.core.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

internal class SchemaContributorIndex : StringStubIndexExtension<SchemaContributor>() {
  override fun getKey(): StubIndexKey<String, SchemaContributor> {
    return KEY
  }

  override fun get(
    key: String,
    project: Project,
    scope: GlobalSearchScope
  ): Collection<SchemaContributor> {
    return StubIndex.getElements<String, SchemaContributor>(getKey(), key,
        project, scope,
        SchemaContributor::class.java)
  }

  companion object {
    internal val KEY by lazy {
      StubIndexKey.createIndexKey<String, SchemaContributor>("sqldelight.schema")
    }

    internal var instance: StringStubIndexExtension<SchemaContributor> = SchemaContributorIndex()
  }
}
