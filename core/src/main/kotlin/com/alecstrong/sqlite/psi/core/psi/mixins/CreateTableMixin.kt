package com.alecstrong.sqlite.psi.core.psi.mixins

import com.alecstrong.sqlite.psi.core.SqliteAnnotationHolder
import com.alecstrong.sqlite.psi.core.psi.LazyQuery
import com.alecstrong.sqlite.psi.core.psi.QueryElement.QueryResult
import com.alecstrong.sqlite.psi.core.psi.QueryElement.SynthesizedColumn
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnName
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElement
import com.alecstrong.sqlite.psi.core.psi.SqliteCompositeElementImpl
import com.alecstrong.sqlite.psi.core.psi.SqliteCompoundSelectStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteCreateTableStmt
import com.alecstrong.sqlite.psi.core.psi.SqliteForeignKeyClause
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.alecstrong.sqlite.psi.core.psi.TableElement
import com.alecstrong.sqlite.psi.core.psi.asColumns
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

internal abstract class CreateTableMixin(
    node: ASTNode
) : SqliteCompositeElementImpl(node),
    SqliteCreateTableStmt,
    TableElement {
  override fun tableExposed() = LazyQuery(tableName) {
    compoundSelectStmt?.let {
      QueryResult(tableName, it.queryExposed().flatMap { it.columns })
    } ?: queryAvailable(this).single()
  }

  override fun queryAvailable(child: PsiElement): List<QueryResult> = analyze("queryAvailable") {
    val synthesizedColumns = if (node.findChildByType(SqliteTypes.WITHOUT) == null) {
      val columnNames = columnDefList.mapNotNull { it.columnName.name }
      listOf(SynthesizedColumn(
          table = this,
          acceptableValues = listOf("rowid", "oid", "_oid_").filter { it !in columnNames }
      ))
    } else {
      emptyList()
    }
    return listOf(QueryResult(
        table = tableName,
        columns = columnDefList.map { it.columnName }.asColumns(),
        synthesizedColumns = synthesizedColumns
    ))
  }

  override fun annotate(annotationHolder: SqliteAnnotationHolder) {
    checkForDuplicateColumns(annotationHolder)
    checkForSubqueries(annotationHolder)
    if (checkForDuplicateColumnConstraints(annotationHolder)) {
      checkPrimaryKey(annotationHolder)
      checkForeignKeys(annotationHolder)
    }
    super.annotate(annotationHolder)
  }

  private fun primaryKey(): List<String> {
    val compositeKey = tableConstraintList.firstOrNull { it.hasPrimaryKey() }
    if (compositeKey != null) {
      return compositeKey.indexedColumnList.map { it.columnName.name }
    }

    return columnDefList.filter { it.columnConstraintList.any { it.hasPrimaryKey() } }
        .take(1)
        .map { it.columnName.name }
  }

  private fun isCollectivelyUnique(columns: List<SqliteColumnName>): Boolean {
    tableConstraintList.filter { it.hasPrimaryKey() || it.isUnique() }
        .map { it.indexedColumnList.mapNotNull { it.columnName.name } }
        .plus(listOf(columnDefList.filter {
          it.columnConstraintList.any { it.hasPrimaryKey() || it.isUnique() }
        }.mapNotNull { it.columnName.name }))
        .forEach { uniqueKeys ->
          if (columns.map { it.name }.all { it in uniqueKeys }) return true
        }

    // Check if there is an externally created unique index that matches the given columns.
    containingFile.indexes()
        .filter { it.isUnique() && it.indexedColumnList.all { it.collationName == null } }
        .forEach {
          val indexedColumns = it.indexedColumnList.map { it.columnName.name }
          if (columns.map { it.name }.containsAll(indexedColumns)
              && columns.size == indexedColumns.size) {
            return true
          }
        }

    return false
  }

  private fun checkForDuplicateColumns(annotationHolder: SqliteAnnotationHolder) {
    columnDefList.map { it.columnName }
        .groupBy { it.name.trim('\'', '"', '`', '[', ']') }
        .map { it.value }
        .filter { it.size > 1 }
        .flatMap { it }
        .forEach {
          annotationHolder.createErrorAnnotation(it, "Duplicate column name")
        }
  }

  private fun checkForeignKeys(annotationHolder: SqliteAnnotationHolder) {
    fun SqliteForeignKeyClause.checkCompositeForeignKey(columns: List<SqliteColumnName>) {
      if (columns.isEmpty()) throw AssertionError()

      val foreignTable = foreignTable.reference?.resolve()?.parent as? CreateTableMixin ?: return
      val foreignKey = foreignTable.primaryKey()

      if (columnNameList.isEmpty()) {
        // Must map to the foreign tables primary key which must be exactly one column long.
        if (columns.size == 1 && foreignKey.size != 1) {
          annotationHolder.createErrorAnnotation(this,
              "Table ${foreignTable.tableName.name} has a composite primary key")
        } else if (columns.size != foreignKey.size) {
          annotationHolder.createErrorAnnotation(this, "Foreign key constraint must match the" +
              " primary key of the foreign table exactly. Constraint has ${columns.size} columns" +
              " and foreign table primary key has ${foreignKey.size} columns")
        }
      } else {
        // The columns specified must be unique.
        if (!foreignTable.isCollectivelyUnique(columnNameList)) {
          if (columnNameList.size == 1) {
            annotationHolder.createErrorAnnotation(this,
                "Table ${foreignTable.tableName.name} does not have a unique index on column ${columnNameList.first().name}")
          } else {
            annotationHolder.createErrorAnnotation(this,
                "Table ${foreignTable.tableName.name} does not have a unique index on columns" +
                    " ${columnNameList.joinToString(prefix = "[", postfix = "]") { it.name }}")
          }
        }
      }
    }

    columnDefList.forEach { column ->
      column.columnConstraintList
          .mapNotNull { it.foreignKeyClause }
          .forEach {
            if (it.columnNameList.size > 1) {
              annotationHolder.createErrorAnnotation(it,
                  "Column can only reference a single foreign key")
            } else {
              it.checkCompositeForeignKey(listOf(column.columnName))
            }
          }
    }

    tableConstraintList.filter { it.foreignKeyClause != null }
        .forEach { constraint ->
          constraint.foreignKeyClause!!.checkCompositeForeignKey(
              constraint.columnNameList
          )
        }
  }

  private fun checkPrimaryKey(annotationHolder: SqliteAnnotationHolder) {
    // Verify there is only a single primary key
    val constraints = columnDefList.flatMap { it.columnConstraintList }
        .filter { it.hasPrimaryKey() }
        .plus(tableConstraintList.filter { it.hasPrimaryKey() })
    if (constraints.size > 1) {
      constraints.forEach {
        annotationHolder.createErrorAnnotation(it,
            "Table ${tableName.name} can only have one primary key")
      }
    }
  }

  private fun checkForSubqueries(annotationHolder: SqliteAnnotationHolder) {
    columnDefList.forEach {
      PsiTreeUtil.findChildOfType(it, SqliteCompoundSelectStmt::class.java)?.let {
        annotationHolder.createErrorAnnotation(it,
            "Subqueries are not permitted as part of CREATE TABLE statements")
      }
    }
  }

  /**
   * @return true if the column constraint lists are all well formed.
   */
  private fun checkForDuplicateColumnConstraints(annotationHolder: SqliteAnnotationHolder): Boolean {
    columnDefList.forEach {
      if (it.columnConstraintList.count { it.hasPrimaryKey() } > 1) {
        annotationHolder.createErrorAnnotation(it, "Duplicate primary key clauses")
        return false
      }
      if (it.columnConstraintList.count { it.isUnique() } > 1) {
        annotationHolder.createErrorAnnotation(it, "Duplicate unique clauses")
        return false
      }
    }
    return true
  }

  companion object {
    private fun SqliteCompositeElement.hasPrimaryKey() = node.findChildByType(SqliteTypes.PRIMARY) != null
    private fun SqliteCompositeElement.isUnique() = node.findChildByType(SqliteTypes.UNIQUE) != null
  }
}