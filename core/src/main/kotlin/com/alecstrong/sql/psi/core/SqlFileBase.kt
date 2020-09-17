package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.NamedElement
import com.alecstrong.sql.psi.core.psi.Schema
import com.alecstrong.sql.psi.core.psi.SchemaContributor
import com.alecstrong.sql.psi.core.psi.SqlCreateTableStmt
import com.alecstrong.sql.psi.core.psi.SqlCreateViewStmt
import com.alecstrong.sql.psi.core.psi.SqlStmt
import com.alecstrong.sql.psi.core.psi.SqlStmtList
import com.alecstrong.sql.psi.core.psi.SqlTableName
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
  private var symbolTable = SymbolTable()

  private var viewNames = setOf(*views().map { it.viewName.name }.toTypedArray())
  private var tableElements = setOf(*tables().toTypedArray())

  private val psiManager: PsiManager
    get() = PsiManager.getInstance(project)

  abstract val order: Int?

  val sqlStmtList
    get() = findChildByClass(SqlStmtList::class.java)

  fun tablesAvailable(sqlStmtElement: PsiElement): Collection<LazyQuery> {
    symbolTable.checkInitialized()
    val statement = (sqlStmtElement as SqlStmt).firstChild
    var symbolTable: MutableMap<TableElement, LazyQuery> = symbolTable.tables
    if (order != null) {
      symbolTable = symbolTable.toMutableMap()
      sqlStmtList!!.stmtList
          .takeWhile { !PsiTreeUtil.isAncestor(it, sqlStmtElement, false) }
          .forEach { symbolTable.applyStatement(it) }
    }
    return if (statement !is TableElement || statement is SqlCreateTableStmt) {
      symbolTable.values
    } else {
      symbolTable.filterKeys { it != statement }.values
    }
  }

  internal inline fun <reified T> schema(sqlStmtElement: PsiElement? = null): Collection<T> {
    val schema = Schema()
    iteratePreviousStatements { statement ->
      if (order != null && sqlStmtElement?.parent == statement) {
        return@schema schema.forType<T>().values()
      }
      when (val contributor = statement.firstChild) {
        is SchemaContributor -> contributor.modifySchema(schema)
      }
    }
    return schema.forType<T>().values()
  }

  /**
   * @return Tables this file exposes as LazyQuery.
   *
   * @param includeAll If true, also return tables that other files expose.
   */
  fun tables(includeAll: Boolean): Collection<LazyQuery> {
    symbolTable.checkInitialized()
    var tables: MutableMap<TableElement, LazyQuery> = symbolTable.tables

    if (order != null) {
      tables = tables.toMutableMap()
      sqlStmtList!!.stmtList.forEach { tables.applyStatement(it) }
    }

    return if (includeAll) {
      tables.values
    } else {
      tables.filterKeys { tableElement ->
        tableElement in sqlStmtList!!.stmtList.mapNotNull { it.firstChild }
      }.values
    }
  }

  /**
   * @return Views this file exposes as CreateViewStmt.
   *
   * @param includeAll If true, also return tables that other files expose.
   */
  fun views(includeAll: Boolean): Collection<SqlCreateViewStmt> {
    symbolTable.checkInitialized()
    var views: MutableMap<String, SqlCreateViewStmt> = symbolTable.views

    if (order != null) {
      views = views.toMutableMap()
      sqlStmtList!!.stmtList.forEach { statement ->
        statement.createViewStmt?.let { views[it.viewName.text] = it }
        statement.dropViewStmt?.viewName?.let { views.remove(it.text) }
      }
    }

    return if (includeAll) {
      views.values
    } else {
      views.filterKeys { viewName ->
        viewName in sqlStmtList!!.stmtList.mapNotNull { it.createViewStmt?.viewName?.text }
      }.values
    }
  }

  internal fun viewForName(name: String): SqlCreateViewStmt? {
    symbolTable.checkInitialized()
    return symbolTable.views[name]
  }

  private fun MutableMap<TableElement, LazyQuery>.applyStatement(
    statement: SqlStmt
  ) {
    fun removeTableForName(name: NamedElement) {
      val iterator = iterator()
      while (iterator.hasNext()) {
        if (iterator.next().key.name().text == name.text) iterator.remove()
      }
    }

    statement.dropViewStmt?.viewName?.let(::removeTableForName)
    statement.dropTableStmt?.let {
      PsiTreeUtil.findChildrenOfType(it, SqlTableName::class.java).forEach(::removeTableForName)
    }
    statement.alterTableStmt?.let { alter ->
      val tableName = alter.tableName ?: return@let
      removeTableForName(tableName)
    }
    (statement.firstChild as? TableElement)?.let { this[it] = it.tableExposed() }
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

  private inline fun iteratePreviousStatements(block: (SqlStmt) -> Unit) {
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

  override fun subtreeChanged() {
    super.subtreeChanged()
    if (parent == null) {
      // Lightweight copy of the original file. Dont do any mods.
      return
    }

    if (order != null) {
      // Files with an order greater than this or null need to recompute entirely.
      iterateSqlFiles {
        if (it.order == null || it.order!! >= order!!) {
          it.symbolTable = SymbolTable()
        }
        return@iterateSqlFiles true
      }
      return
    }

    val newViews = views()
    val newTables = tables()
    iterateSqlFiles { psiFile ->
      viewNames.forEach { psiFile.symbolTable.views.remove(it) }
      tableElements.forEach { psiFile.symbolTable.tables.remove(it) }

      psiFile.symbolTable.views.putAll(newViews.map { it.viewName.name to it })
      psiFile.symbolTable.tables.putAll(newTables.map { it to it.tableExposed() })

      return@iterateSqlFiles true
    }

    viewNames = setOf(*newViews.map { it.viewName.name }.toTypedArray())
    tableElements = setOf(*newTables.toTypedArray())
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

  /**
   * Creates a copy of this file keeping all external symbols intact.
   */
  fun copyWithSymbols(): SqlFileBase {
    val copy = copy() as SqlFileBase

    copy.symbolTable.views.putAll(symbolTable.views.filterKeys { it !in viewNames })
    copy.symbolTable.tables.putAll(symbolTable.tables.filterKeys { it !in tableElements })

    copy.symbolTable.views.putAll(copy.views().map { it.viewName.name to it })
    copy.symbolTable.tables.putAll(copy.tables().map { it to it.tableExposed() })

    copy.symbolTable.initialized = true
    return copy
  }

  inner class SymbolTable {
    internal val views = LinkedHashMap<String, SqlCreateViewStmt>()
    internal val tables = LinkedHashMap<TableElement, LazyQuery>()
    internal var initialized = false

    fun checkInitialized() {
      synchronized(initialized) {
        if (initialized) return

        iteratePreviousStatements { statement ->
          if (statement.containingFile == this@SqlFileBase &&
              (order != null || statement.alterTableStmt != null)) {
            return@iteratePreviousStatements
          }
          tables.applyStatement(statement)
          statement.createViewStmt?.let { views[it.viewName.text] = it }
          statement.dropViewStmt?.viewName?.let { views.remove(it.text) }
        }

        initialized = true
      }
    }
  }
}
