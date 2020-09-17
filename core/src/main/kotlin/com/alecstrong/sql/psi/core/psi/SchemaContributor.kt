package com.alecstrong.sql.psi.core.psi

import com.intellij.util.containers.MultiMap
import kotlin.reflect.KClass

internal interface SchemaContributor {
  fun modifySchema(schema: Schema)
}

internal class Schema {
  private val map = mutableMapOf<KClass<*>, MultiMap<String, *>>()

  inline fun <reified T> forType(): MultiMap<String, T> {
    var value = map[T::class]
    if (value == null) {
      value = MultiMap<String, T>()
      map[T::class] = value
    }

    @Suppress("UNCHECKED_CAST")
    return value as MultiMap<String, T>
  }
}
