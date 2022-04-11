CREATE TABLE foo (
  id INTEGER NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  selected INTEGER NOT NULL
);

CREATE TABLE bar (
  id INTEGER NOT NULL PRIMARY KEY,
  short_name TEXT NOT NULL,
  full_name TEXT NOT NULL,
  selected INTEGER NOT NULL
);

CREATE VIEW foobar AS
SELECT id, name, selected FROM foo
UNION
SELECT id, short_name AS name, selected FROM bar;

CREATE TRIGGER foobar_update_added
INSTEAD OF UPDATE OF selected ON foobar
BEGIN
  UPDATE foo SET selected = new.selected WHERE id = new.id;
  UPDATE bar SET selected = new.selected WHERE id = new.id;
END;

UPDATE OR IGNORE foobar SET selected = ? WHERE id = ?;