CREATE TABLE test(
  text1 TEXT,
  text2 TEXT,
  text3 TEXT
);

UPDATE test
SET (text1, text2, text3) = ('one', 'two', 'three');