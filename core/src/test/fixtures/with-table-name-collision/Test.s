CREATE TABLE test (
  _id INTEGER NOT NULL
);

WITH test AS (
  VALUES (1)
)
SELECT *
FROM test;