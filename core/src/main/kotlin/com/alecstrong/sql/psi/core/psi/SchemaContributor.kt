package com.alecstrong.sql.psi.core.psi

import com.intellij.util.containers.MultiMap
import kotlin.reflect.KClass

internal interface SchemaContributor {
  fun modifySchema(schema: Schema)
}

internal class Schema {
  private val map = mutableMapOf<KClass<*>, MultiMap<String, *>>()

  @Suppress("UNCHECKED_CAST")
  inline fun <reified T> forType() =
      map.getOrPut(T::class, { MultiMap<String, T>() }) as MultiMap<String, T>
}
