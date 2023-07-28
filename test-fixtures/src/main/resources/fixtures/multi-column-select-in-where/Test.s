CREATE TABLE foo(
  id INTEGER NOT NULL,
  name TEXT NOT NULL
);

SELECT *
FROM foo
WHERE (id, name) IN ?;

SELECT *
FROM foo
WHERE (id, name) IN (?, ?);
