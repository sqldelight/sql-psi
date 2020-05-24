package com.alecstrong.sql.psi.core.psi.mixins

import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sql.psi.core.psi.QueryElement.SynthesizedColumn
import com.alecstrong.sql.psi.core.psi.SqlCompositeElementImpl
import com.alecstrong.sql.psi.core.psi.SqlCreateVirtualTableStmt
import com.alecstrong.sql.psi.core.psi.SqlModuleArgument
import com.alecstrong.sql.psi.core.psi.TableElement
import com.alecstrong.sql.psi.core.psi.asColumns
import com.intellij.lang.ASTNode

internal abstract class CreateVirtualTableMixin(
  node: ASTNode
) : SqlCompositeElementImpl(node),
    SqlCreateVirtualTableStmt,
    TableElement {
  override fun name() = tableName

  override fun tableExposed(): LazyQuery {
    val columnNameElements = findChildrenByClass(
        SqlModuleArgument::class.java)
        .mapNotNull { it.moduleArgumentDef?.moduleColumnDef?.columnName }

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

val SqlCreateVirtualTableStmt.usesFtsModule: Boolean
  get() = this.moduleName?.text?.startsWith(prefix = "fts", ignoreCase = true) == true
