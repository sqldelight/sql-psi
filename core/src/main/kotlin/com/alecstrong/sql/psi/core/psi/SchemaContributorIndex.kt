package com.alecstrong.sql.psi.core.psi

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey

internal interface SchemaContributorIndex {
  fun getKey(): StubIndexKey<String, SchemaContributor>
  fun get(key: String, project: Project, scope: GlobalSearchScope): Collection<SchemaContributor>

  companion object {
    internal val KEY by lazy {
      StubIndexKey.createIndexKey<String, SchemaContributor>("sqldelight.schema")
    }

    fun getInstance(project: Project): SchemaContributorIndex {
      return ServiceManager.getService(project, SchemaContributorIndex::class.java)
        ?: SchemaContributorIndexImpl.instance
    }
  }
}

internal class SchemaContributorIndexImpl : SchemaContributorIndex, StringStubIndexExtension<SchemaContributor>() {
  override fun getVersion() = 3

  override fun getKey(): StubIndexKey<String, SchemaContributor> {
    return SchemaContributorIndex.KEY
  }

  override fun get(
    key: String,
    project: Project,
    scope: GlobalSearchScope
  ): Collection<SchemaContributor> {
    if (DumbService.isDumb(project)) return emptyList()
    return StubIndex.getElements<String, SchemaContributor>(
      getKey(), key,
      project, scope,
      SchemaContributor::class.java
    )
  }

  companion object {
    val instance: SchemaContributorIndexImpl = SchemaContributorIndexImpl()
  }
}
