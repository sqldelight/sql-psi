package com.alecstrong.sql.psi.core

import com.intellij.lang.Language
import com.intellij.psi.tree.IElementType

class SqlElementType(name: String) : IElementType(name, null) {
  override fun getLanguage(): Language = _language

  companion object {
    /**
     * Not my favourite hack of all time but the language can't be static since its provided at
     * "runtime".
     */
    var _language: Language = Language.ANY
  }
}
