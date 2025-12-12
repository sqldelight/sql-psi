package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.AnnotationException
import com.alecstrong.sql.psi.core.SqlAnnotationHolder
import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SchemaContributorStubImpl
import com.alecstrong.sql.psi.core.psi.SqlAlterTableRules
import com.alecstrong.sql.psi.core.psi.SqlAlterTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.impl.SqlAlterTableStmtImpl
import com.alecstrong.sql.psi.core.psi.withAlterStatement
import com.intellij.lang.ASTNode
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil.getParentOfType

interface AlterTableStmtStub : SchemaContributorStub {
  fun newTableName(): String?
}

internal class AlterTableStmtStubImpl<T : TableElement>(
  parent: StubElement<*>?,
  type: SqlSchemaContributorElementType<T>,
  name: String,
  textOffset: Int,
  private val newTableName: String?,
) : SchemaContributorStubImpl<T>(parent, type, name, textOffset), AlterTableStmtStub {
  override fun newTableName() = newTableName
}

internal abstract class AlterTableMixin
private constructor(stub: AlterTableStmtStub?, nodeType: IElementType?, node: ASTNode?) :
  SqlSchemaContributorImpl<TableElement, AlterTableElementType>(stub, nodeType, node),
  SqlAlterTableStmt,
  TableElement {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(stub: AlterTableStmtStub, nodeType: IElementType) : this(stub, nodeType, null)

  override fun getStub() = super.getStub() as AlterTableStmtStub?

  fun newTableName(): String? {
    stub?.let {
      return it.newTableName()
    }
    return newTableName?.name
  }

  override fun name(): String {
    stub?.let {
      return it.name()
    }
    return tableName.name
  }

  override fun modifySchema(schema: Schema) {
    schema.forType<TableElement>().remove(name())
    schema.forType<TableElement>().put(newTableName() ?: name(), this)
  }

  override fun queryAvailable(child: PsiElement): Collection<QueryElement.QueryResult> {
    if (child in alterTableRulesList) {
      check(child is SqlAlterTableRules)
      return tablesAvailable(this)
        .filter { it.tableName.textMatches(tableName) }
        .map { it.withAlterStatement(this, until = child).query }
    }
    return super.queryAvailable(child)
  }

  override fun tableExposed(): LazyQuery {
    return LazyQuery(
      tableName = newTableName ?: tableName,
      query = result@{
          val tableName =
            getParentOfType(tableName.reference?.resolve(), TableElement::class.java)
              ?: return@result QueryElement.QueryResult(columns = emptyList())
          val lazyQuery = tableName.tableExposed()
          try {
              lazyQuery.withAlterStatement(this)
            } catch (e: AnnotationException) {
              lazyQuery
            }
            .query
        },
    )
  }

  override fun annotate(annotationHolder: SqlAnnotationHolder) {
    if (containingFile.order == null) {
      annotationHolder.createErrorAnnotation(
        this,
        "Alter table statements are forbidden outside of migration files.",
      )
      return
    }
    val parent = getParentOfType(tableName.reference?.resolve(), TableElement::class.java)
    when (parent) {
      is SqlAlterTableStmt,
      is SqlCreateTableStmt -> {}
      else -> {
        annotationHolder.createErrorAnnotation(
          tableName,
          "Attempting to alter something that is not a table.",
        )
        return
      }
    }
    try {
      parent.tableExposed().withAlterStatement(this)
    } catch (e: AnnotationException) {
      annotationHolder.createErrorAnnotation(e.element ?: this, e.msg)
    }
    super.annotate(annotationHolder)
  }
}

open class AlterTableElementType(name: String) :
  SqlSchemaContributorElementType<TableElement>(name, TableElement::class.java) {
  override fun nameType() = SqlTypes.TABLE_NAME

  override fun createPsi(stub: SchemaContributorStub) =
    SqlAlterTableStmtImpl(stub as AlterTableStmtStub, this)

  override fun serialize(stub: SchemaContributorStub, stubOutputStream: StubOutputStream) {
    super.serialize(stub, stubOutputStream)
    stubOutputStream.writeName((stub as AlterTableStmtStub).newTableName())
  }

  override fun deserialize(
    stubStream: StubInputStream,
    parentStub: StubElement<*>?,
  ): SchemaContributorStub {
    return AlterTableStmtStubImpl(
      parentStub,
      this,
      stubStream.readNameString()!!,
      stubStream.readInt(),
      stubStream.readNameString(),
    )
  }

  override fun createStub(
    contributor: SchemaContributor,
    parentStub: StubElement<*>?,
  ): SchemaContributorStub {
    return AlterTableStmtStubImpl(
      parentStub,
      this,
      contributor.name(),
      contributor.textOffset,
      (contributor as SqlAlterTableStmt).newTableName?.name,
    )
  }

  override fun createStub(
    tree: LighterAST,
    node: LighterASTNode,
    parentStub: StubElement<*>,
  ): SchemaContributorStub {
    val name = LightTreeUtil.firstChildOfType(tree, node, nameType()).toString()
    val newName = LightTreeUtil.firstChildOfType(tree, node, SqlTypes.NEW_TABLE_NAME)?.toString()
    return AlterTableStmtStubImpl(parentStub, this, name, node.startOffset, newName)
  }
}

private val SqlAlterTableStmt.newTableName
  get() = alterTableRulesList.mapNotNull { it.alterTableRenameTable?.newTableName }.lastOrNull()
