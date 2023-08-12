CREATE TABLE test (
  _id TEXT NOT NULL PRIMARY KEY
);

SELECT *
FROM test AS one
JOIN (
  SELECT *
  FROM test
  WHERE test._id = one._id
);