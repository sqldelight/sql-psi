CREATE TABLE test(
  value TEXT
);

ALTER TABLE test RENAME TO new_test;

SELECT *
-- error[col 5]: No table found with name test
FROM test;

SELECT value
FROM new_test;

ALTER TABLE new_test ADD COLUMN value2 TEXT;

SELECT *
-- error[col 5]: No table found with name test
FROM test;

SELECT value, value2
FROM new_test;

ALTER TABLE new_test ADD COLUMN value3 TEXT;

SELECT *
-- error[col 5]: No table found with name test
FROM test;

SELECT value, value2, value3
FROM new_test;
