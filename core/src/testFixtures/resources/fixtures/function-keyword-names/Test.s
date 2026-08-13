CREATE TABLE foo (
  value TEXT
);

SELECT REPLACE(value, 'foo', 'bar')
FROM foo;

SELECT LEFT(value, 2)
FROM foo;

SELECT RIGHT(value, 2)
FROM foo;

SELECT LIKE(value, 'foo')
FROM foo;

SELECT GLOB('*oo', value)
FROM foo;

SELECT IF(value = 'foo', 'yes', 'no')
FROM foo;

SELECT MATCH(value, 'foo')
FROM foo;

SELECT REGEXP('f.o', value)
FROM foo;
