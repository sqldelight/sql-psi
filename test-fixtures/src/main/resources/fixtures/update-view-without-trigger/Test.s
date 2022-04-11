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

-- error[col 17]: Cannot UPDATE the view foobar without a trigger on foobar that has INSTEAD OF UPDATE.
UPDATE OR IGNORE foobar SET selected = ? WHERE id = ?;