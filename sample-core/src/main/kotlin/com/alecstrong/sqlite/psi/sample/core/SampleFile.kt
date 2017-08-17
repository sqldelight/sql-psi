package com.alecstrong.sqlite.psi.sample.core

import com.alecstrong.sqlite.psi.core.psi.SqliteSqlStmt
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil

class SampleFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, SampleLanguage) {
  override fun getFileType() = SampleFileType
  override fun toString() = "Sample File"
  fun sqlStmts(): Collection<SqliteSqlStmt> = PsiTreeUtil.findChildrenOfType(this, SqliteSqlStmt::class.java)
}