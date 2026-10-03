CREATE TABLE foo(
    id TEXT NOT NULL PRIMARY KEY,
    bar TEXT NOT NULL
);

SELECT /* Random comment saying something */ *
FROM foo;

SELECT /* Random comment saying something with extra star **/ *
FROM foo;

SELECT *
FROM /* Random comment saying something */ foo;

SELECT /* Multi line
block comment
full of words */ *
FROM foo;

SELECT /*vt+ SOME_FANCY_FEATURE*/ *
FROM foo;

INSERT /*Random comment saying something*/
INTO foo
VALUES (1,'something');

UPDATE /*Some comment*/ foo
SET bar='baz'
WHERE id=1;

DELETE /*Comment*/
FROM foo
WHERE id=1;
