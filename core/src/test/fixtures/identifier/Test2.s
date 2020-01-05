CREATE TABLE identifier_without_whitespace(id INTEGER PRIMARY KEY);

-- It doesn't matter here that these 3 identifiers are the same, because checks
-- for duplicate tables are done in the SqlDelight layer.
CREATE TABLE "identifier with whitespace"(id INTEGER PRIMARY KEY);
CREATE TABLE `identifier with whitespace`(id INTEGER PRIMARY KEY);
CREATE TABLE [identifier with whitespace](id INTEGER PRIMARY KEY);

CREATE TABLE `5`(id INTEGER PRIMARY KEY);
CREATE TABLE `$^&%*(")`(id INTEGER PRIMARY KEY);
CREATE TABLE `.`(id INTEGER PRIMARY KEY);
CREATE TABLE "To be, or not to be. That is the question?"(id INTEGER PRIMARY KEY);

-- error[col 19]: <column name real> expected, got '5'
CREATE TABLE test1(5a INTEGER PRIMARY KEY);