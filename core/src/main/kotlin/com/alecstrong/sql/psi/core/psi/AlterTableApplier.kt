package com.alecstrong.sql.psi.core.psi

import com.alecstrong.sql.psi.core.AnnotationException
import com.alecstrong.sql.psi.core.mysql.psi.MySqlAlterTableRules
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

internal interface AlterTableApplier : PsiElement {
  fun applyTo(lazyQuery: LazyQuery): LazyQuery
}

internal val AlterTableApplier.alterStmt
  get() = PsiTreeUtil.getParentOfType(
      this,
      SqlAlterTableStmt::class.java
  )!!

internal fun LazyQuery.withAlterStatement(
  alter: SqlAlterTableStmt,
  until: SqlAlterTableRules? = null
): LazyQuery {
  return alter.alterTableRulesList.takeWhile { it != until }.fold(this, { lazyQuery, alterTableRules ->
    // Rename table.
    alterTableRules.alterTableRenameTable?.let { renameTable ->
      return@fold LazyQuery(
          tableName = renameTable.newTableName,
          query = {
            lazyQuery.query.copy(table = renameTable.newTableName)
          }
      )
    }

    (alterTableRules.firstChild as? AlterTableApplier)?.let {
      return@fold it.applyTo(lazyQuery)
    }

    (alterTableRules as? MySqlAlterTableRules)?.alterTableAddIndex?.let {
      return@fold lazyQuery
    }

    (alterTableRules as? MySqlAlterTableRules)?.alterTableDropIndex?.let {
      return@fold lazyQuery
    }

    throw AnnotationException("Unhandled alter rule", alterTableRules)
  })
}
