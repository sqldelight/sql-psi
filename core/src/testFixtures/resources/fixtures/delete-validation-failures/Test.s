CREATE TABLE test (
  _id INTEGER NOT NULL
);

DELETE FROM test
WHERE fake_column = 1;

-- valid.
WITH temp_table AS (
  VALUES (1), (2), (3)
)
DELETE FROM test
WHERE _id IN temp_table;

WITH temp_table AS (
  VALUES (1, 2, 3)
)
DELETE FROM test
WHERE _id IN temp_table;

DELETE FROM test
WHERE _id IN temp_table;