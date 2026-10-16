CREATE TABLE foo(
    id TEXT NOT NULL PRIMARY KEY,
    bar TEXT NOT NULL
);

-- Leading, before the statement starts.
/* Leading block comment */
SELECT *
FROM foo;

-- Block comment after verb.
SELECT /* Random comment saying something */ *
FROM foo;

-- Bonus star in comment.
SELECT /* Random comment saying something with extra star **/ *
FROM foo;

-- Comment after FROM.
SELECT *
FROM /* Random comment saying something */ foo;

-- Multi line block comment.
SELECT /* Multi line
block comment
full of words */ *
FROM foo;

-- Comment with insert.
INSERT /*Random comment saying something*/
INTO foo
VALUES (1,'something');

-- Comment with update.
UPDATE /*Some comment*/ foo
SET bar='baz'
WHERE id=1;

-- Comment with delete.
DELETE /*Comment*/
FROM foo
WHERE id=1;

-- Empty comment, no padding.
SELECT /**/ *
FROM foo;

-- A line comment inside a block comment must not swallow the rest of the file.
SELECT /* -- not a line comment
         still inside the block */ *
FROM foo;

-- An unbalanced quote inside a comment must not open a string literal.
SELECT /* don't terminate the statement */ *
FROM foo;

-- Block comments do not nest: the first */ closes it, so ` *` is the result column.
SELECT /* /* only one level */ *
FROM foo;

-- Extra leading stars are a block comment, not a javadoc.
SELECT /*** three stars ***/ *
FROM foo;

-- Between an operand and its operator.
SELECT *
FROM foo
WHERE id /* here too */ = 'a';

-- Trailing, immediately before the semicolon.
SELECT *
FROM foo /* trailing */;
