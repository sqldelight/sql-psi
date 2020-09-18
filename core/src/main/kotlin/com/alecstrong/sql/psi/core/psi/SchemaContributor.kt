package com.alecstrong.sql.psi.core.psi

import com.intellij.util.containers.MultiMap
import kotlin.reflect.KClass

internal interface SchemaContributor {
  fun modifySchema(schema: Schema)
}

internal class Schema {
  private val map = mutableMapOf<KClass<*>, MultiMap<*, *>>()

  @Suppress("UNCHECKED_CAST")
  inline fun <Key, reified Value> forType() =
      map.getOrPut(Value::class, { MultiMap<Key, Value>() }) as MultiMap<Key, Value>

  @Suppress("UNCHECKED_CAST")
  inline fun <reified Value> values() =
      map[Value::class]?.values() as Collection<Value>? ?: emptyList()
}

internal fun MultiMap<TableElement, LazyQuery>.removeTableForName(name: NamedElement) {
  keySet().filter { it.name().text == name.text }.forEach { remove(it) }
}
