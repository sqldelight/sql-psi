CREATE TABLE test (
  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  other_column INTEGER NOT NULL
);

SELECT *
FROM test
WHERE _id IN (
  SELECT _id
  FROM test
);

SELECT *
FROM test
WHERE _id IN (
  SELECT _id, other_column
  FROM test
);

INSERT INTO test(other_column) VALUES (TRUE), (FALSE);
