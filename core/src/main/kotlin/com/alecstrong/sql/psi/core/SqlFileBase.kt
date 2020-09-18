package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlStmt
import com.alecstrong.sql.psi.core.psi.SqlStmtList
import com.alecstrong.sql.psi.core.psi.TableElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

abstract class SqlFileBase(
  viewProvider: FileViewProvider,
  language: Language
) : PsiFileBase(viewProvider, language) {
  private val psiManager: PsiManager
    get() = PsiManager.getInstance(project)

  abstract val order: Int?

  val sqlStmtList
    get() = findChildByClass(SqlStmtList::class.java)

  fun tablesAvailable(child: PsiElement) = schema<LazyQuery>(child)

  fun triggers(sqlStmtElement: PsiElement?): Collection<SqlCreateTriggerStmt> = schema(sqlStmtElement)

  internal inline fun <reified T> schema(
    sqlStmtElement: PsiElement? = null,
    includeAll: Boolean = true
  ): Collection<T> {
    val schema = Schema()
    iteratePreviousStatements(includeAll) { statement ->
      val sqlStatement = statement.firstChild
      if (sqlStmtElement != null && PsiTreeUtil.isAncestor(statement, sqlStmtElement, false)) {
        if (order == null && (sqlStatement is TableElement && sqlStatement !is SqlCreateTableStmt)) {
          // If we're in a queries file, the table is not available to itself (unless its a create).
          return@iteratePreviousStatements
        }
        if (order != null) {
          // If we're in a migration file, only return the tables up to this point.
          return@schema schema.values()
        }
      }

      if (sqlStatement is SchemaContributor) {
        sqlStatement.modifySchema(schema)
      }
    }
    return schema.values()
  }

  /**
   * @return Tables this file exposes as LazyQuery.
   *
   * @param includeAll If true, also return tables that other files expose.
   */
  fun tables(includeAll: Boolean): Collection<LazyQuery> {
    val tables = schema<LazyQuery>()
    return if (includeAll) tables else tables.filter { it.tableName.containingFile == this }
  }

  /**
   * @return Views this file exposes as CreateViewStmt.
   *
   * @param includeAll If true, also return tables that other files expose.
   */
  fun views(includeAll: Boolean): Collection<SqlCreateViewStmt> {
    val views = schema<SqlCreateViewStmt>()
    return if (includeAll) views else views.filter { it.containingFile == this }
  }

  internal fun viewForName(name: String): SqlCreateViewStmt? {
    return schema<SqlCreateViewStmt>().singleOrNull { it.name().text == name }
  }

  private fun views(): List<SqlCreateViewStmt> {
    return sqlStmtList?.stmtList?.mapNotNull {
      it.createViewStmt
    }.orEmpty()
  }

  private fun tables(): List<TableElement> {
    return sqlStmtList?.stmtList?.mapNotNull {
      it.createViewStmt ?: it.createTableStmt ?: it.createVirtualTableStmt
    }.orEmpty()
  }

  private inline fun iteratePreviousStatements(
    includeAll: Boolean = true,
    block: (SqlStmt) -> Unit
  ) {
    if (!includeAll) {
      sqlStmtList?.stmtList?.forEach(block)
      return
    }

    val files = sortedMapOf<Int, SqlFileBase>()
    val topFiles = LinkedHashSet<SqlFileBase>()
    iterateSqlFiles { file ->
      if (file == this) return@iterateSqlFiles true
      else if (order != null && file.order == null) return@iterateSqlFiles true
      else if (order == null && file.order == null) topFiles.add(file)
      else if (order == null || (file.order != null && file.order!! < order!!)) files[file.order!!] = file

      return@iterateSqlFiles true
    }

    files.forEach { (_, file) ->
      file.sqlStmtList?.stmtList?.forEach(block)
    }
    topFiles.forEach { it.sqlStmtList?.stmtList?.forEach(block) }
    sqlStmtList?.stmtList?.forEach(block)
  }

  protected open fun iterateSqlFiles(iterator: (SqlFileBase) -> Boolean) {
    ProjectRootManager.getInstance(project).fileIndex.iterateContent { file ->
      if (file.fileType != fileType) return@iterateContent true
      psiManager.findFile(file)?.let { psiFile ->
        return@iterateContent iterator(psiFile as SqlFileBase)
      }
      true
    }
  }
}
