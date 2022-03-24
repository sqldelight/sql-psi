CREATE TABLE test (
  _id INTEGER NOT NULL
);

SELECT *
FROM test
WHERE _id = ?1 AND _id = ?1;