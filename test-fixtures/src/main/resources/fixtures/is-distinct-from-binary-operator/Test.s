CREATE TABLE is_distinct_from_test (
  value TEXT,
  value2 TEXT
);

SELECT *
FROM is_distinct_from_test
WHERE value IS DISTINCT FROM value2;

SELECT *
FROM is_distinct_from_test
WHERE value IS NOT DISTINCT FROM value2;

SELECT *
FROM is_distinct_from_test
WHERE value IS DISTINCT FROM NULL;

SELECT *
FROM is_distinct_from_test
WHERE value IS NOT DISTINCT FROM NULL;

SELECT *
FROM is_distinct_from_test
WHERE value IS DISTINCT FROM ?;

SELECT *
FROM is_distinct_from_test
WHERE value IS NOT DISTINCT FROM ?;
