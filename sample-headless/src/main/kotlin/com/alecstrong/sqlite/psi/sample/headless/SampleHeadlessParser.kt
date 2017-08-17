package com.alecstrong.sqlite.psi.sample.headless

import com.alecstrong.sqlite.psi.core.CustomSqliteParser
import com.alecstrong.sqlite.psi.core.SqliteCoreEnvironment
import com.alecstrong.sqlite.psi.sample.core.SampleFile
import com.alecstrong.sqlite.psi.sample.core.SampleFileType
import com.alecstrong.sqlite.psi.sample.core.SampleLanguage
import com.alecstrong.sqlite.psi.sample.core.SampleParser
import com.alecstrong.sqlite.psi.sample.core.SampleParserDefinition
import com.alecstrong.sqlite.psi.sample.core.SampleTypes
import com.alecstrong.sqlite.psi.sample.core.psi.ColumnDef
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase.Parser
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import java.io.File

class SampleHeadlessParser {
  fun parseSqlite() {
    val parserDefinition = SampleParserDefinition()
    val environment = SqliteCoreEnvironment(parserDefinition, SampleFileType)
    val psiManager = PsiManager.getInstance(environment.projectEnvironment.project)

    parserDefinition.setParserOverride(object : CustomSqliteParser() {
      override fun columnDef(builder: PsiBuilder, level: Int, column_def: Parser): Boolean {
        return SampleParser.column_def(builder, level)
      }

      override fun createElement(node: ASTNode): PsiElement {
        return try {
          SampleTypes.Factory.createElement(node)
        } catch (e: AssertionError) {
          super.createElement(node)
        }
      }
    })

    val localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
    val sourceRoots = "sample-headless/src"
    File(sourceRoots).walkTopDown()
        .filter { it.isFile }
        .forEach { file ->
          val virtualFile = localFileSystem.findFileByPath(file.absolutePath)
          if (virtualFile != null && virtualFile.extension?.equals(SampleFileType.defaultExtension) == true) {
            val psiFile = psiManager.findFile(virtualFile) as? SampleFile ?: return@forEach
            printPsi(psiFile, ::println)
            println((psiFile.sqlStmts().first().createTableStmt!!.columnDefList.first() as ColumnDef).javaType!!.text)
          }
        }

    val file = PsiFileFactory.getInstance(environment.projectEnvironment.project)
        .createFileFromText(SampleLanguage, "SELECT * FROM test;")
    printPsi(file, ::println)
  }

  private fun printPsi(psiElement: PsiElement, printer: (String) -> Unit) {
    printer(psiElement.toString())
    psiElement.children.forEach {
      printPsi(it, { printer("  $it") } )
    }
  }
}

fun main(args: Array<String>) {
  SampleHeadlessParser().parseSqlite()
  /* Outputs:
    Sample File
      SqliteSqlStmtListImpl(SQL_STMT_LIST)
        SqliteSqlStmtImpl(SQL_STMT)
          SqliteCreateTableStmtImpl(CREATE_TABLE_STMT)
            SqliteTableNameImpl(TABLE_NAME)
            ColumnDefImpl(COLUMN_DEF)
              SqliteColumnNameImpl(COLUMN_NAME)
              SqliteTypeImpl(SQLITE_TYPE)
              JavaTypeImpl(JAVA_TYPE)
              SqliteColumnConstraintImpl(COLUMN_CONSTRAINT)
                SqliteConflictClauseImpl(CONFLICT_CLAUSE)
        SqliteSqlStmtImpl(SQL_STMT)
          SqliteSelectStmtImpl(SELECT_STMT)
            SqliteResultColumnImpl(RESULT_COLUMN)
            SqliteTableOrSubqueryImpl(TABLE_OR_SUBQUERY)
              SqliteTableNameImpl(TABLE_NAME)
            SqliteBinaryExprImpl(BINARY_EXPR)
              SqliteColumnExprImpl(COLUMN_EXPR)
                SqliteColumnNameImpl(COLUMN_NAME)
              SqliteBindExprImpl(BIND_EXPR)
                SqliteBindParameterImpl(BIND_PARAMETER)
    "java.util.List"
    Sample File
      SqliteSqlStmtListImpl(SQL_STMT_LIST)
        SqliteSqlStmtImpl(SQL_STMT)
          SqliteSelectStmtImpl(SELECT_STMT)
            SqliteResultColumnImpl(RESULT_COLUMN)
            SqliteTableOrSubqueryImpl(TABLE_OR_SUBQUERY)
              SqliteTableNameImpl(TABLE_NAME)
   */
}