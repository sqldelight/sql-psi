SELECT stuffs
FROM test;

CREATE TABLE test (
  stuff INTEGER NOT NULL REFERENCES not_a_table
);