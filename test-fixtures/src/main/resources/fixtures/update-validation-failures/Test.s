CREATE TABLE test (
  some_column INTEGER NOT NULL
);

UPDATE test
SET fake_column = 2;

WITH temp_table AS (
  SELECT count(*) AS total_count
  FROM (VALUES (1), (2), (3), (4))
)
UPDATE test
SET some_column = (
  SELECT total_count FROM temp_table
)
WHERE fake_column = 2;
