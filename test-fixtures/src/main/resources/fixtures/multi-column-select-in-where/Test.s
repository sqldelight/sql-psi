CREATE TABLE foo(
  id INTEGER NOT NULL,
  col_one TEXT NOT NULL,
  col_two TEXT NOT NULL
);

CREATE TABLE bar(
  id INTEGER NOT NULL,
  x TEXT NOT NULL,
  y TEXT NOT NULL
);


SELECT *
FROM foo
WHERE (id, col_one) IN ?;

SELECT *
FROM foo
WHERE (col_one, col_two) IN ( SELECT x, y FROM bar );
