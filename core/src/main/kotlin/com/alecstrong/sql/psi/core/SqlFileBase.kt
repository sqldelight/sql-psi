package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SchemaContributorIndex
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateTriggerStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlStmtList
import com.alecstrong.sql.psi.core.psi.TableElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

abstract class SqlFileBase(
  viewProvider: FileViewProvider,
  language: Language
) : PsiFileBase(viewProvider, language) {
  abstract val order: Int?

  val sqlStmtList
    get() = findChildByClass(SqlStmtList::class.java)

  fun tablesAvailable(child: PsiElement) = schema<TableElement>(child).map { it.tableExposed() }

  fun triggers(sqlStmtElement: PsiElement?): Collection<SqlCreateTriggerStmt> = schema(sqlStmtElement)

  internal inline fun <reified T : SchemaContributor> schema(
    sqlStmtElement: PsiElement? = null,
    includeAll: Boolean = true
  ): Collection<T> {
    val schema = Schema()
    (originalFile as SqlFileBase).iteratePreviousStatements<T>(sqlStmtElement, includeAll) { statement ->
      if (sqlStmtElement != null && PsiTreeUtil.isAncestor(sqlStmtElement, statement, false)) {
        if (order == null && (statement is TableElement && statement !is SqlCreateTableStmt)) {
          // If we're in a queries file, the table is not available to itself (unless its a create).
          return@iteratePreviousStatements
        }
        if (order != null) {
          // If we're in a migration file, only return the tables up to this point.
          return@schema schema.values()
        }
      }

      statement.modifySchema(schema)
    }
    return schema.values()
  }

  /**
   * @return Tables this file exposes as LazyQuery.
   *
   * @param includeAll If true, also return tables that other files expose.
   */
  fun tables(includeAll: Boolean): Collection<LazyQuery> {
    val tables = schema<TableElement>().map { it.tableExposed() }
    return if (includeAll) tables else tables.filter { it.tableName.containingFile == this }
  }

  internal fun viewForName(name: String): SqlCreateViewStmt? {
    return schema<TableElement>().filterIsInstance<SqlCreateViewStmt>().singleOrNull { it.name() == name }
  }

  private fun views(): List<SqlCreateViewStmt> {
    return sqlStmtList?.stmtList?.mapNotNull {
      it.createViewStmt
    }.orEmpty()
  }

  private fun tables(): List<TableElement> {
    return sqlStmtList?.stmtList?.mapNotNull {
      (it.createViewStmt as TableElement?) ?: it.createTableStmt ?: it.createVirtualTableStmt
    }.orEmpty()
  }

  private inline fun <reified T : SchemaContributor> iteratePreviousStatements(
    until: PsiElement?,
    includeAll: Boolean = true,
    block: (SchemaContributor) -> Unit
  ) {
    if (includeAll) {
      val orderedContributors = sortedMapOf<Int, LinkedHashSet<SchemaContributor>>()
      val topContributors = LinkedHashSet<SchemaContributor>()
      val index = SchemaContributorIndex.getInstance(project)

      index.get(T::class.java.name, project, searchScope()).forEach {
        val file = it.containingFile

        if (file == this) return@forEach
        else if (order != null && file.order == null) return@forEach
        else if (order == null && file.order == null) topContributors.add(it)
        else if (order == null || (file.order != null && file.order!! < order!!)) {
          orderedContributors.getOrPut(file.order!!, { linkedSetOf() }).add(it)
        }
      }

      orderedContributors.forEach { (_, contributors) ->
        contributors.sortedBy { it.textOffset }.forEach(block)
      }
      topContributors.forEach(block)
    }

    sqlStmtList?.stmtList?.mapNotNull { it.firstChild as? SchemaContributor }
        ?.takeWhile { order == null || until == null || it.textOffset <= until.textOffset }
        ?.forEach {
          block(it)
        }
  }

  protected open fun searchScope(): GlobalSearchScope {
    return GlobalSearchScope.everythingScope(project)
  }
}
