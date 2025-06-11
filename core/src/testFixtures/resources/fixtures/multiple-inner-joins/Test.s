CREATE TABLE testTable(
  testColumn INTEGER
);

SELECT test_column IS NOT NULL
FROM (
  SELECT testColumn AS test_column
  FROM testTable
)
LEFT JOIN (SELECT 1);