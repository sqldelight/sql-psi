CREATE TABLE test(
  value TEXT
);

SELECT *
FROM test
LEFT JOIN (
  SELECT value
  FROM test
) AS alias
ON (test.value = alias.value);