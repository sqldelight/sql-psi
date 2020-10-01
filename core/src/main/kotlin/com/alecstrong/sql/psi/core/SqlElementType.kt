package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SchemaContributorIndex
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SchemaContributorStubImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.lang.Language
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.ILightStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IElementType

class SqlElementType(name: String) : IElementType(name, null) {
  override fun getLanguage(): Language = _language

  companion object {
    /**
     * Not my favourite hack of all time but the language can't be static since its provided at
     * "runtime".
     */
    var _language: Language = Language.ANY
  }
}

internal abstract class SqlSchemaContributorElementType<SchemaType : SchemaContributor>(
  private val name: String,
  /**
   * This should be the same class used when interfacing with [com.alecstrong.sql.psi.core.psi.Schema]
   */
  private val schemaClass: Class<SchemaType>
) : ILightStubElementType<SchemaContributorStub, SchemaContributor>(name, null) {
  /**
   * This should be the token type for the unique name, for example [SqlTypes.INDEX_NAME] for
   * CREATE_INDEX or DROP_INDEX
   */
  abstract fun nameType(): IElementType

  override fun getLanguage() = SqlElementType._language

  override fun getExternalId() = "sqldelight.$name"

  override fun serialize(stub: SchemaContributorStub, stubOutputStream: StubOutputStream) {
    stubOutputStream.writeName(stub.name())
    stubOutputStream.writeInt(stub.getTextOffset())
  }

  override fun deserialize(
    stubStream: StubInputStream,
    parentStub: StubElement<*>?
  ): SchemaContributorStub {
    return SchemaContributorStubImpl(parentStub, this, stubStream.readNameString()!!,
        stubStream.readInt())
  }

  override fun createStub(
    contributor: SchemaContributor,
    parentStub: StubElement<*>?
  ): SchemaContributorStub {
    return SchemaContributorStubImpl(parentStub, this, contributor.name(), contributor.textOffset)
  }

  override fun indexStub(stub: SchemaContributorStub, sink: IndexSink) {
    sink.occurrence(SchemaContributorIndex.KEY, schemaClass.name)
  }

  override fun createStub(
    tree: LighterAST,
    node: LighterASTNode,
    parentStub: StubElement<*>
  ): SchemaContributorStub {
    val name = LightTreeUtil.firstChildOfType(tree, node, nameType()).toString()
    return SchemaContributorStubImpl(parentStub, this, name, node.startOffset)
  }
}
