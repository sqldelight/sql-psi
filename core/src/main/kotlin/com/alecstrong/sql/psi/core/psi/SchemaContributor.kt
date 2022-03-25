package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import kotlin.reflect.KClass

interface SchemaContributor : SqlCompositeElement {
  fun modifySchema(schema: Schema)
  fun name(): String
}

internal interface SchemaContributorStub : StubElement<SchemaContributor> {
  fun name(): String
  fun getTextOffset(): Int
}

internal open class SchemaContributorStubImpl<T : SchemaContributor>(
  parent: StubElement<*>?,
  type: SqlSchemaContributorElementType<T>,
  private val name: String,
  private val textOffset: Int
) : StubBase<SchemaContributor>(parent, type),
  SchemaContributorStub {
  override fun name() = name
  override fun getTextOffset() = textOffset
}

class Schema {
  private val map = mutableMapOf<KClass<out SchemaContributor>, MutableMap<String, out SchemaContributor>>()

  @Suppress("UNCHECKED_CAST")
  internal inline fun <reified Value : SchemaContributor> forType(): MutableMap<String, Value> =
    map.getOrPut(Value::class, { linkedMapOf<String, Value>() }) as MutableMap<String, Value>

  @Suppress("UNCHECKED_CAST")
  internal inline fun <reified Value : SchemaContributor> put(value: SchemaContributor) {
    val map = map.getOrPut(Value::class, { linkedMapOf<String, Value>() }) as MutableMap<String, SchemaContributor>
    map.putIfAbsent(value.name(), value)
  }

  @Suppress("UNCHECKED_CAST")
  fun <Value : SchemaContributor> values(type: KClass<Value>) =
    map[type]?.values as Collection<Value>? ?: emptyList()
}
