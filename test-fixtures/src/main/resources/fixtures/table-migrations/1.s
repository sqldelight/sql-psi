CREATE TABLE test(
  id TEXT NOT NULL
);

-- error[col 21]: No column found with name new_column
INSERT INTO test(id, new_column)
VALUES ('hello', 'world');