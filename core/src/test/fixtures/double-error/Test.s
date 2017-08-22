CREATE TABLE test (
  _id INTEGER NOT NULL PRIMARY KEY
);

CREATE VIEW view_with_failure AS
SELECT fake_column
FROM test;

SELECT *
FROM view_with_failure;

SELECT *
FROM view_with_failure;