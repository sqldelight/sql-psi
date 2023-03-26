package com.alecstrong.sql.psi.core.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.alecstrong.sql.psi.core.psi.SqlTypes.*;

%%

%{
  public SqlLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class SqlLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

WHITE_SPACE=\s+

COMMENT=--.*
JAVADOC="/"\*\*([^*]|\*+[^/*])*\*"/"
DIGIT=[0-9]+(\.[0-9]*)?
ID=([a-zA-Z_][a-zA-Z_0-9]*)|(`[a-zA-Z_0-9 ]+`)|(\[[a-zA-Z_0-9 ]+\])
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
  "DO"                   { return DO; }
  "NOTHING"              { return NOTHING; }
  "WINDOW"               { return WINDOW; }
  "PARTITION"            { return PARTITION; }
  "MERGE"                { return MERGE; }
  "VALUE"                { return VALUE; }
  "LOCK"                 { return LOCK; }
  "USE"                  { return USE; }
  "EXPLAIN"              { return EXPLAIN; }
  "QUERY"                { return QUERY; }
  "PLAN"                 { return PLAN; }
  "ALTER"                { return ALTER; }
  "TABLE"                { return TABLE; }
  "ADD"                  { return ADD; }
  "COLUMN"               { return COLUMN; }
  "RENAME"               { return RENAME; }
  "TO"                   { return TO; }
  "ANALYZE"              { return ANALYZE; }
  "ATTACH"               { return ATTACH; }
  "DATABASE"             { return DATABASE; }
  "AS"                   { return AS; }
  "BEGIN"                { return BEGIN; }
  "DEFERRED"             { return DEFERRED; }
  "IMMEDIATE"            { return IMMEDIATE; }
  "EXCLUSIVE"            { return EXCLUSIVE; }
  "TRANSACTION"          { return TRANSACTION; }
  "COMMIT"               { return COMMIT; }
  "END"                  { return END; }
  "ROLLBACK"             { return ROLLBACK; }
  "SAVEPOINT"            { return SAVEPOINT; }
  "RELEASE"              { return RELEASE; }
  "CREATE"               { return CREATE; }
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
  "CONSTRAINT"           { return CONSTRAINT; }
  "PRIMARY"              { return PRIMARY; }
  "KEY"                  { return KEY; }
  "AUTOINCREMENT"        { return AUTOINCREMENT; }
  "NULL"                 { return NULL; }
  "GENERATED"            { return GENERATED; }
  "ALWAYS"               { return ALWAYS; }
  "CHECK"                { return CHECK; }
  "DEFAULT"              { return DEFAULT; }
  "FOREIGN"              { return FOREIGN; }
  "REFERENCES"           { return REFERENCES; }
  "DELETE"               { return DELETE; }
  "UPDATE"               { return UPDATE; }
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
  "INSERT"               { return INSERT; }
  "FOR"                  { return FOR; }
  "EACH"                 { return EACH; }
  "ROW"                  { return ROW; }
  "WHEN"                 { return WHEN; }
  "VIEW"                 { return VIEW; }
  "VIRTUAL"              { return VIRTUAL; }
  "USING"                { return USING; }
  "WITH"                 { return WITH; }
  "RECURSIVE"            { return RECURSIVE; }
  "FROM"                 { return FROM; }
  "ORDER"                { return ORDER; }
  "BY"                   { return BY; }
  "LIMIT"                { return LIMIT; }
  "OFFSET"               { return OFFSET; }
  "DETACH"               { return DETACH; }
  "DROP"                 { return DROP; }
  "AND"                  { return AND; }
  "OR"                   { return OR; }
  "DISTINCT"             { return DISTINCT; }
  "SEPARATOR"            { return SEPARATOR; }
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
  "TRUE"                 { return TRUE; }
  "FALSE"                { return FALSE; }
  "E"                    { return E; }
  "INTO"                 { return INTO; }
  "VALUES"               { return VALUES; }
  "PRAGMA"               { return PRAGMA; }
  "REINDEX"              { return REINDEX; }
  "SELECT"               { return SELECT; }
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
  "WITHOUT"              { return WITHOUT; }
  "ROWID"                { return ROWID; }

  {COMMENT}              { return COMMENT; }
  {JAVADOC}              { return JAVADOC; }
  {DIGIT}                { return DIGIT; }
  {ID}                   { return ID; }
  {STRING}               { return STRING; }

}

[^] { return BAD_CHARACTER; }
