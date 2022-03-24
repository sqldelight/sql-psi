CREATE TABLE test (
  _id INTEGER NOT NULL PRIMARY KEY
);

CREATE VIEW view1(first_name,second_name)
AS
SELECT *
FROM test;
