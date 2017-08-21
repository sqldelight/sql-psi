package com.alecstrong.sqlite.psi.core

import com.alecstrong.sqlite.psi.core.lexer.SqliteLexer
import com.intellij.lexer.FlexAdapter

internal class SqliteLexerAdapter: FlexAdapter(SqliteLexer())