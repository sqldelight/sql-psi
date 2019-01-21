package com.alecstrong.sqlite.psi.core.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.alecstrong.sqlite.psi.core.psi.SqliteTypes.*;

%%

%{
  public SqliteLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class SqliteLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

SPACE=[ \t\n\x0B\f\r]+
COMMENT=--.*
JAVADOC="/"\*\*(.|\n)*\*"/"
DIGIT=[0-9]+(\.[0-9]*)?
ID=([a-zA-Z0-9_\`\[\]])*
STRING=('([^'])*'|\"([^\"])*\")

%%
<YYINITIAL> {
  {WHITE_SPACE}          { return WHITE_SPACE; }

  ";"                    { return SEMI; }
  "="                    { return EQ; }
  "("                    { return LP; }
  ")"                    { return RP; }
  "."                    { return DOT; }
  ","                    { return COMMA; }
  "+"                    { return PLUS; }
  "-"                    { return MINUS; }
  "~"                    { return BITWISE_NOT; }
  ">>"                   { return SHIFT_RIGHT; }
  "<<"                   { return SHIFT_LEFT; }
  "<"                    { return LT; }
  ">"                    { return GT; }
  "<="                   { return LTE; }
  ">="                   { return GTE; }
  "=="                   { return EQ2; }
  "!="                   { return NEQ; }
  "<>"                   { return NEQ2; }
  "*"                    { return MULTIPLY; }
  "/"                    { return DIVIDE; }
  "%"                    { return MOD; }
  "&"                    { return BITWISE_AND; }
  "|"                    { return BITWISE_OR; }
  "||"                   { return CONCAT; }
  "EXPLAIN"              { return EXPLAIN; }
  "QUERY"                { return QUERY; }
  "PLAN"                 { return PLAN; }
  "ALTER"                { return ALTER; }
  "ANALYZE"              { return ANALYZE; }
  "ATTACH"               { return ATTACH; }
  "BEGIN"                { return BEGIN; }
  "COMMIT"               { return COMMIT; }
  "END"                  { return END; }
  "ROLLBACK"             { return ROLLBACK; }
  "SAVEPOINT"            { return SAVEPOINT; }
  "RELEASE"              { return RELEASE; }
  "CREATE"               { return CREATE; }
  "DROP"                 { return DROP; }
  "INSERT"               { return INSERT; }
  "WITH"                 { return WITH; }
  "UPDATE"               { return UPDATE; }
  "DELETE"               { return DELETE; }
  "SELECT"               { return SELECT; }
  "PRAGMA"               { return PRAGMA; }
  "REINDEX"              { return REINDEX; }
  "TABLE"                { return TABLE; }
  "RENAME"               { return RENAME; }
  "TO"                   { return TO; }
  "ADD"                  { return ADD; }
  "COLUMN"               { return COLUMN; }
  "DATABASE"             { return DATABASE; }
  "AS"                   { return AS; }
  "DEFERRED"             { return DEFERRED; }
  "IMMEDIATE"            { return IMMEDIATE; }
  "EXCLUSIVE"            { return EXCLUSIVE; }
  "TRANSACTION"          { return TRANSACTION; }
  "UNIQUE"               { return UNIQUE; }
  "INDEX"                { return INDEX; }
  "IF"                   { return IF; }
  "NOT"                  { return NOT; }
  "EXISTS"               { return EXISTS; }
  "ON"                   { return ON; }
  "WHERE"                { return WHERE; }
  "COLLATE"              { return COLLATE; }
  "ASC"                  { return ASC; }
  "DESC"                 { return DESC; }
  "TEMP"                 { return TEMP; }
  "TEMPORARY"            { return TEMPORARY; }
  "WITHOUT"              { return WITHOUT; }
  "ROWID"                { return ROWID; }
  "CONSTRAINT"           { return CONSTRAINT; }
  "PRIMARY"              { return PRIMARY; }
  "KEY"                  { return KEY; }
  "AUTOINCREMENT"        { return AUTOINCREMENT; }
  "NULL"                 { return NULL; }
  "CHECK"                { return CHECK; }
  "DEFAULT"              { return DEFAULT; }
  "FOREIGN"              { return FOREIGN; }
  "REFERENCES"           { return REFERENCES; }
  "SET"                  { return SET; }
  "CASCADE"              { return CASCADE; }
  "RESTRICT"             { return RESTRICT; }
  "NO"                   { return NO; }
  "ACTION"               { return ACTION; }
  "MATCH"                { return MATCH; }
  "DEFERRABLE"           { return DEFERRABLE; }
  "INITIALLY"            { return INITIALLY; }
  "CONFLICT"             { return CONFLICT; }
  "ABORT"                { return ABORT; }
  "FAIL"                 { return FAIL; }
  "IGNORE"               { return IGNORE; }
  "REPLACE"              { return REPLACE; }
  "TRIGGER"              { return TRIGGER; }
  "BEFORE"               { return BEFORE; }
  "AFTER"                { return AFTER; }
  "INSTEAD"              { return INSTEAD; }
  "OF"                   { return OF; }
  "FOR"                  { return FOR; }
  "EACH"                 { return EACH; }
  "ROW"                  { return ROW; }
  "WHEN"                 { return WHEN; }
  "VIEW"                 { return VIEW; }
  "VIRTUAL"              { return VIRTUAL; }
  "USING"                { return USING; }
  "RECURSIVE"            { return RECURSIVE; }
  "FROM"                 { return FROM; }
  "ORDER"                { return ORDER; }
  "BY"                   { return BY; }
  "LIMIT"                { return LIMIT; }
  "OFFSET"               { return OFFSET; }
  "DETACH"               { return DETACH; }
  "AND"                  { return AND; }
  "OR"                   { return OR; }
  "DISTINCT"             { return DISTINCT; }
  "CAST"                 { return CAST; }
  "LIKE"                 { return LIKE; }
  "GLOB"                 { return GLOB; }
  "REGEXP"               { return REGEXP; }
  "ESCAPE"               { return ESCAPE; }
  "ISNULL"               { return ISNULL; }
  "NOTNULL"              { return NOTNULL; }
  "IS"                   { return IS; }
  "BETWEEN"              { return BETWEEN; }
  "IN"                   { return IN; }
  "CASE"                 { return CASE; }
  "THEN"                 { return THEN; }
  "ELSE"                 { return ELSE; }
  "RAISE"                { return RAISE; }
  "CURRENT_TIME"         { return CURRENT_TIME; }
  "CURRENT_DATE"         { return CURRENT_DATE; }
  "CURRENT_TIMESTAMP"    { return CURRENT_TIMESTAMP; }
  "E"                    { return E; }
  "INTO"                 { return INTO; }
  "VALUES"               { return VALUES; }
  "ALL"                  { return ALL; }
  "GROUP"                { return GROUP; }
  "HAVING"               { return HAVING; }
  "INDEXED"              { return INDEXED; }
  "NATURAL"              { return NATURAL; }
  "LEFT"                 { return LEFT; }
  "OUTER"                { return OUTER; }
  "INNER"                { return INNER; }
  "CROSS"                { return CROSS; }
  "JOIN"                 { return JOIN; }
  "UNION"                { return UNION; }
  "INTERSECT"            { return INTERSECT; }
  "EXCEPT"               { return EXCEPT; }
  "VACUUM"               { return VACUUM; }

  {SPACE}                { return SPACE; }
  {COMMENT}              { return COMMENT; }
  {JAVADOC}              { return JAVADOC; }
  {DIGIT}                { return DIGIT; }
  {ID}                   { return ID; }
  {STRING}               { return STRING; }

}

[^] { return BAD_CHARACTER; }
