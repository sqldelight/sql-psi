CREATE TABLE test (
  _id INTEGER NOT NULL
);

SELECT _id, _id INTO ?, ? FROM test;
