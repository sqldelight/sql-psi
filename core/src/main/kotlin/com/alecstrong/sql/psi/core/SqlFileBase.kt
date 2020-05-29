package com.alecstrong.sql.psi.core

import androidx.collection.SparseArrayCompat
import androidx.collection.forEach
import com.alecstrong.sql.psi.core.psi.LazyQuery
import com.alecstrong.sql.psi.core.psi.NamedElement
import com.alecstrong.sql.psi.core.psi.SqlCreateIndexStmt
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
import com.intellij.util.containers.MultiMap

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

  open fun tablesAvailable(sqlStmtElement: PsiElement): Collection<LazyQuery> {
    symbolTable.checkInitialized()
    val statement = (sqlStmtElement as SqlStmt).children.first()
    var queries: Collection<LazyQuery>

    if (statement !is TableElement || statement is SqlCreateTableStmt) {
      queries = symbolTable.tables.values
    } else {
      queries = symbolTable.tables.filterKeys { it != statement }.values
    }

    if (order != null) {
      // Gotta do some fancy shit to apply all the statements leading up to [sqlStmtElement]
      sqlStmtList?.stmtList?.filterNotNull()?.forEach { statement ->
        if (PsiTreeUtil.isAncestor(statement, sqlStmtElement, false)) return queries
        statement.createTableStmt?.let { queries += it.tableExposed() }
        statement.createVirtualTableStmt?.let { queries += it.tableExposed() }
        statement.createViewStmt?.let { queries += it.tableExposed() }
        statement.dropTableStmt?.let {
          queries = queries.filterNot { query -> it.tableName?.text == query.tableName.text }
        }
        statement.dropViewStmt?.let {
          queries = queries.filterNot { query -> it.viewName?.text == query.tableName.text }
        }

        statement.alterTableStmt?.let { alter ->
          queries = queries.map {
            if (it.tableName.text == alter.tableName.text) it.withAlterStatement(alter)
            else it
          }
        }
      }
    }

    return queries
  }

  open fun indexes(sqlStmtElement: PsiElement): Collection<SqlCreateIndexStmt> {
    val result = MultiMap<String, SqlCreateIndexStmt>()
    iteratePreviousStatements { statement ->
      if (order != null && sqlStmtElement.parent == statement) {
        return@indexes result.values()
      }
      statement.createIndexStmt?.let { result.putValue(it.indexName.text, it) }
      statement.dropIndexStmt?.let { result.remove(it.indexName?.text) }
    }
    return result.values()
  }

  open fun triggers(sqlStmtElement: PsiElement): Collection<SqlCreateTriggerStmt> {
    val result = MultiMap<String, SqlCreateTriggerStmt>()
    iteratePreviousStatements { statement ->
      if (order != null && sqlStmtElement.parent == statement) {
        return@triggers result.values()
      }
      statement.createTriggerStmt?.let { result.putValue(it.triggerName.text, it) }
      statement.dropTriggerStmt?.let { result.remove(it.triggerName?.text) }
    }
    return result.values()
  }

  internal fun viewForName(name: String): SqlCreateViewStmt? {
    symbolTable.checkInitialized()
    return symbolTable.views[name]
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
    val files = SparseArrayCompat<SqlFileBase>()
    val topFiles = LinkedHashSet<SqlFileBase>()
    iterateSqlFiles { file ->
      if (file == this) return@iterateSqlFiles true
      else if (order != null && file.order == null) return@iterateSqlFiles true
      else if (order == null && file.order == null) topFiles.add(file)
      else if (order == null || (file.order != null && file.order!! < order!!)) files.put(file.order!!, file)

      return@iterateSqlFiles true
    }

    files.forEach { _, file ->
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

    private fun removeTableForName(name: NamedElement) {
      val iterator = tables.iterator()
      while (iterator.hasNext()) {
        if (iterator.next().key.name().text == name.text) iterator.remove()
      }
    }

    fun checkInitialized() {
      synchronized(initialized) {
        if (initialized) return

        iteratePreviousStatements { statement ->
          if (order != null && statement.containingFile == this@SqlFileBase) {
            return@iteratePreviousStatements
          }
          statement.createViewStmt?.let {
            views[it.viewName.name] = it
            tables[it] = it.tableExposed()
          }
          statement.dropViewStmt?.let {
            views.remove(it.viewName?.name)
            it.viewName?.let(::removeTableForName)
          }

          statement.createVirtualTableStmt?.let { tables[it] = it.tableExposed() }
          statement.createTableStmt?.let { tables[it] = it.tableExposed() }
          statement.alterTableStmt?.let { alter ->
            removeTableForName(alter.tableName)
            tables[alter] = alter.tableExposed()
          }
          statement.dropTableStmt?.tableName?.let(::removeTableForName)
        }

        initialized = true
      }
    }
  }
}
