CREATE TABLE SimpleTable (
  id INTEGER NOT NULL PRIMARY KEY,
  text TEXT NOT NULL
);

CREATE TRIGGER delete_before_insert
BEFORE INSERT ON SimpleTable
WHEN (SELECT count(*) FROM SimpleTable) > 1000
BEGIN
    DELETE FROM SimpleTable WHERE id = (SELECT min(id) FROM SimpleTable);
END;

CREATE TRIGGER update_before_insert
BEFORE INSERT ON SimpleTable
WHEN (SELECT count(*) FROM SimpleTable) > 1000
BEGIN
    UPDATE SimpleTable SET text = 'stuff' WHERE id = (SELECT min(id) FROM SimpleTable);
END;