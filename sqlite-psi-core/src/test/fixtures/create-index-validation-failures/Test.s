CREATE TABLE test (
  _id INTEGER NOT NULL,
  column1 INTEGER NOT NULL
);

CREATE INDEX index_1
ON test (fake_column);

-- valid.
CREATE INDEX index_2
ON test (_id)
WHERE column1 IS NOT NULL;

-- valid.
CREATE INDEX index_3
ON test (_id COLLATE some_collation_name ASC);