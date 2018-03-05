package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.QueryElement.SynthesizedColumn
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnName
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateVirtualTableStmt
import com.alecstrong.sqlite.psi.core.psi.TableElement
import com.intellij.lang.ASTNode

internal abstract class CreateVirtualTableMixin(
  node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateVirtualTableStmt,
    TableElement {
  override fun tableExposed(): LazyQuery {
    val synthesizedColumns = if (moduleName.text.startsWith("fts")) {
      listOf(
          SynthesizedColumn(
              table = this,
              acceptableValues = listOf("docid", "rowid", "oid", "_oid_")
          )
      )
    } else {
      emptyList()
    }
    return LazyQuery(tableName) {
      QueryResult(
          table = tableName,
          columns = findChildrenByClass(SqliteColumnName::class.java).asList(),
          synthesizedColumns = synthesizedColumns
      )
    }
  }
}