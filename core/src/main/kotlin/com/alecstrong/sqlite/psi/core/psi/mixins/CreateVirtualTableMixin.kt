package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.QueryElement.SynthesizedColumn
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateVirtualTableStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteModuleArgument
import com.alecstrong.sqlite.psi.core.psi.TableElement
import com.alecstrong.sqlite.psi.core.psi.asColumns
import com.intellij.lang.ASTNode

internal abstract class CreateVirtualTableMixin(
  node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateVirtualTableStmt,
    TableElement {
  override fun tableExposed(): LazyQuery {
    val columnNameElements = findChildrenByClass(SqliteModuleArgument::class.java)
        .mapNotNull { it.columnDef?.columnName }

    val synthesizedColumns = if (usesFtsModule) {
      val columnNames = columnNameElements.map { it.name }

      listOf(
          SynthesizedColumn(
              table = this,
              acceptableValues = listOf("docid", "rowid", "oid", "_rowid_", tableName.name)
                  .filter { it !in columnNames }
          )
      )
    } else {
      emptyList()
    }

    return LazyQuery(tableName) {
      QueryResult(
          table = tableName,
          columns = columnNameElements.asColumns(),
          synthesizedColumns = synthesizedColumns
      )
    }
  }
}

val SqliteCreateVirtualTableStmt.usesFtsModule: Boolean
  get() = this.moduleName?.text?.startsWith(prefix ="fts", ignoreCase = true) == true