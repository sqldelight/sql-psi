CREATE TABLE a (
  name TEXT NOT NULL
);

CREATE TABLE b (
  name TEXT NOT NULL
);

CREATE TABLE c (
  name TEXT NOT NULL
);

SELECT a.name
FROM a
JOIN c ON a.name = c.name
UNION
SELECT b.name
FROM b
ORDER BY name;