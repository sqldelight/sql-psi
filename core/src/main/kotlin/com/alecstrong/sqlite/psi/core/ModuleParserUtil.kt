package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes.MODULE_ARGUMENT
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase

abstract class ModuleParserUtil : GeneratedParserUtilBase() {
  companion object {
    @JvmStatic fun custom_module_argument(builder: PsiBuilder, level: Int, columnName: Parser): Boolean {
      if (!GeneratedParserUtilBase.recursion_guard_(builder, level, "module_argument_real")) return false
      var result: Boolean
      val marker = GeneratedParserUtilBase.enter_section_(
          builder, level, GeneratedParserUtilBase._COLLAPSE_, MODULE_ARGUMENT,
          "<module argument real>"
      )
      columnName.parse(builder, level + 1)
      var parens = 0
      while (builder.tokenType != SqliteTypes.COMMA) {
        if (builder.tokenType == SqliteTypes.LP) parens++
        if (builder.tokenType == SqliteTypes.RP && (--parens == -1)) break
        builder.advanceLexer()
      }
      result = (parens <= 0)
      GeneratedParserUtilBase.exit_section_(builder, level, marker, result, false, null)
      return result
    }
  }
}
