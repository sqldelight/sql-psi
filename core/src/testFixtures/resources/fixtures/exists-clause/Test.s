CREATE TABLE test (
  value TEXT,
  value2 TEXT
);

SELECT *
FROM test
WHERE EXISTS (SELECT * FROM test);