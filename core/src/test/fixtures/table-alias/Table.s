CREATE TABLE test (
  _id INTEGER NOT NULL PRIMARY KEY
);

-- works fine.
SELECT test2.*
FROM test AS test2
WHERE test2._id = ?;

-- errors.
SELECT test2.*
FROM test
WHERE test2._id = ?;
