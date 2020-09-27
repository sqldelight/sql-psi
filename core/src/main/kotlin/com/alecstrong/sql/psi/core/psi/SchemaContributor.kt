package com.alecstrong.sql.psi.core.psi

import com.intellij.util.containers.MultiMap
import kotlin.reflect.KClass

internal interface SchemaContributor : SqlCompositeElement {
  fun modifySchema(schema: Schema)
}

internal class Schema {
  private val map = mutableMapOf<KClass<out SchemaContributor>, MultiMap<String, out SchemaContributor>>()

  @Suppress("UNCHECKED_CAST")
  inline fun <reified Value : SchemaContributor> forType(): MultiMap<String, Value> =
      map.getOrPut(Value::class, { MultiMap<String, Value>() }) as MultiMap<String, Value>

  @Suppress("UNCHECKED_CAST")
  inline fun <reified Value : SchemaContributor> values() =
      map[Value::class]?.values() as Collection<Value>? ?: emptyList()
}
