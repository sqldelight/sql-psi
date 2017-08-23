CREATE TABLE test (
  _id INTEGER NOT NULL PRIMARY KEY,
  column1 TEXT DEFAULT 'sup',
  column2 BLOB NOT NULL,
  column3 REAL
);

-- Works fine
INSERT INTO test VALUES (?, ?, ?, ?);

-- Also works fine. Nullable values receive default value of null.
INSERT INTO test (_id, column1, column2) VALUES (?, ?, ?);

-- Works fine, nullable and default value columns are okay.
INSERT INTO test (_id, column2) VALUES (?, ?);

-- Fails as column2 must be specified.
INSERT INTO test (_id, column1, column3) VALUES (?, ?, ?);

-- Fails as _id must be specified.
INSERT INTO test(column1, column2, column3) VALUES (?, ?, ?);

-- Fails since not all columns can have default values.
INSERT INTO test DEFAULT VALUES;