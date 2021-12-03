package com.alecstrong.sql.psi.core

import com.alecstrong.sql.psi.core.lexer.SqlLexer
import com.intellij.lexer.FlexAdapter

class SqlLexerAdapter : FlexAdapter(SqlLexer())
