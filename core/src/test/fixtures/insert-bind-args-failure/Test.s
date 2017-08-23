CREATE TABLE test (
  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  column1 TEXT NOT NULL UNIQUE,
  column2 TEXT NOT NULL,
  column3 TEXT NOT NULL,
  column4 TEXT,
  column5 BLOB,
  column6 INTEGER NOT NULL DEFAULT 0,
  column7 INTEGER NOT NULL DEFAULT 0,
  column8 TEXT,
  column9 INTEGER
);

INSERT INTO test (column1, column2, column3, column4, column5, column6, column7, column8, column9)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

INSERT INTO test (column1, column2)
VALUES (?, ?);

INSERT INTO test (column1, column2, column3)
VALUES (?, ?, ?),
       (?, ?, 'sup'),
       (?, ?);