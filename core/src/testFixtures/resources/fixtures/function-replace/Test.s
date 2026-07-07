CREATE TABLE foo (
  value TEXT
);

SELECT REPLACE(value, 'foo', 'bar')
FROM foo;
