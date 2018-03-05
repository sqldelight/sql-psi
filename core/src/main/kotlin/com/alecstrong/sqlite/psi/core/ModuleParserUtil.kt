package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase

abstract class ModuleParserUtil : GeneratedParserUtilBase() {
  companion object {
    @JvmStatic fun module_argument(builder: PsiBuilder, level: Int, columnName: Parser): Boolean {
      val marker = enter_section_(builder)
      var result = true
      result = result && columnName.parse(builder, level)
      var parens = 0
      while (builder.tokenType != SqliteTypes.COMMA) {
        if (builder.tokenType == SqliteTypes.LP) parens++
        if (builder.tokenType == SqliteTypes.RP && (--parens == -1)) break
        builder.advanceLexer()
      }
      result = result && (parens <= 0)
      exit_section_(builder, marker, null, result)
      return result
    }
  }
}
