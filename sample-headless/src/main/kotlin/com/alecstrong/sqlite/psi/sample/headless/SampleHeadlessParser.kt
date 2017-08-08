package com.alecstrong.sqlite.psi.sample.headless

import com.alecstrong.sqlite.psi.core.SqliteCoreEnvironment
import com.alecstrong.sqlite.psi.sample.core.SampleFileType
import com.alecstrong.sqlite.psi.sample.core.SampleParserDefinition
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import java.io.File

class SampleHeadlessParser {
  fun parseSqlite() {
    val environment = SqliteCoreEnvironment(SampleParserDefinition(), SampleFileType)

    val localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
    val sourceRoots = "sample-headless/src"
    File(sourceRoots).walkTopDown()
        .filter { it.isFile }
        .forEach { file ->
          val virtualFile = localFileSystem.findFileByPath(file.absolutePath)
          if (virtualFile != null && virtualFile.extension?.equals(SampleFileType.defaultExtension) == true) {
            val psiFile = PsiManager.getInstance(environment.projectEnvironment.project)
                .findFile(virtualFile) ?: return@forEach
            printPsi(psiFile, ::println)
          }
        }
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
      SqliteSqlStmtImpl(SQL_STMT)
        SqliteCreateTableStmtImpl(CREATE_TABLE_STMT)
          SqliteTableNameImpl(TABLE_NAME)
          SqliteColumnDefImpl(COLUMN_DEF)
            SqliteColumnNameImpl(COLUMN_NAME)
            SqliteTypeNameImpl(TYPE_NAME)
              SqliteIdentifierImpl(IDENTIFIER)
            SqliteColumnConstraintImpl(COLUMN_CONSTRAINT)
              SqliteConflictClauseImpl(CONFLICT_CLAUSE)
      PsiElement(;)
      PsiWhiteSpace
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
      PsiElement(;)
   */
}