CREATE TABLE test (
  test_column INTEGER NOT NULL
);

SELECT *
FROM test2
JOIN (
  SELECT *
  FROM test2
  WHERE test_column = test2_column
);