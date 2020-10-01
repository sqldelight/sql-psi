package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.util.containers.MultiMap
import kotlin.reflect.KClass

internal interface SchemaContributor : SqlCompositeElement {
  fun modifySchema(schema: Schema)
  fun name(): String
}

internal interface SchemaContributorStub : StubElement<SchemaContributor> {
  fun name(): String
  fun getTextOffset(): Int
}

internal class SchemaContributorStubImpl<T : SchemaContributor>(
  parent: StubElement<*>?,
  type: SqlSchemaContributorElementType<T>,
  private val name: String,
  private val textOffset: Int
) : StubBase<SchemaContributor>(parent, type),
    SchemaContributorStub {
  override fun name() = name
  override fun getTextOffset() = textOffset
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
