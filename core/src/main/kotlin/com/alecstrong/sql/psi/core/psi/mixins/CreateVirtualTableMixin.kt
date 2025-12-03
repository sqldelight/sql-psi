package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.SqlSchemaContributorElementType
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.QueryElement.SynthesizedColumn
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributorStub
import com.alecstrong.sql.psi.core.psi.SqlCreateVirtualTableStmt
import com.alecstrong.sql.psi.core.psi.SqlModuleArgument
import com.alecstrong.sql.psi.core.psi.SqlSchemaContributorImpl
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.asColumns
import com.alecstrong.sql.psi.core.psi.impl.SqlCreateVirtualTableStmtImpl
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

internal abstract class CreateVirtualTableMixin(
  stub: SchemaContributorStub?,
  nodeType: IElementType?,
  node: ASTNode?,
) :
  SqlSchemaContributorImpl<TableElement, CreateVirtualTableElementType>(stub, nodeType, node),
  SqlCreateVirtualTableStmt,
  TableElement {
  constructor(node: ASTNode) : this(null, null, node)

  constructor(stub: SchemaContributorStub, nodeType: IElementType) : this(stub, nodeType, null)

  override fun name(): String {
    stub?.let {
      return it.name()
    }
    return tableName.name
  }

  override fun modifySchema(schema: Schema) {
    schema.put<TableElement>(this)
  }

  override fun tableExposed(): LazyQuery {
    val columnNameElements =
      findChildrenByClass(SqlModuleArgument::class.java).mapNotNull {
        it.moduleArgumentDef?.moduleColumnDef?.columnName
          ?: it.moduleArgumentDef?.columnDef?.columnName
      }

    val synthesizedColumns =
      when {
        usesFts3Module || usesFts4Module -> {
          val columnNames = columnNameElements.map { it.name }

          listOf(
            SynthesizedColumn(
              table = this,
              acceptableValues =
                listOf("docid", "rowid", "oid", "_rowid_", tableName.name).filter {
                  it !in columnNames
                },
            )
          )
        }
        usesFts5Module -> {
          val columnNames = columnNameElements.map { it.name }

          listOf(
            SynthesizedColumn(
              table = this,
              acceptableValues =
                listOf("rowid", "_row_id_", "rank", tableName.name).filter { it !in columnNames },
            )
          )
        }

        else -> emptyList()
      }

    return LazyQuery(tableName) {
      QueryResult(
        table = tableName,
        columns = columnNameElements.asColumns(),
        synthesizedColumns = synthesizedColumns,
      )
    }
  }
}

internal class CreateVirtualTableElementType(name: String) :
  SqlSchemaContributorElementType<TableElement>(name, TableElement::class.java) {
  override fun nameType() = SqlTypes.TABLE_NAME

  override fun createPsi(stub: SchemaContributorStub) = SqlCreateVirtualTableStmtImpl(stub, this)
}

val SqlCreateVirtualTableStmt.usesFtsModule: Boolean
  get() = this.moduleName?.text?.startsWith(prefix = "fts", ignoreCase = true) == true

val SqlCreateVirtualTableStmt.usesFts3Module: Boolean
  get() = this.moduleName?.text?.startsWith(prefix = "fts3", ignoreCase = true) == true

val SqlCreateVirtualTableStmt.usesFts4Module: Boolean
  get() = this.moduleName?.text?.startsWith(prefix = "fts4", ignoreCase = true) == true

val SqlCreateVirtualTableStmt.usesFts5Module: Boolean
  get() = this.moduleName?.text?.startsWith(prefix = "fts5", ignoreCase = true) == true
