CREATE TABLE hockey_player (
  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  number INTEGER NOT NULL,
  team INTEGER,
  age INTEGER NOT NULL,
  weight REAL NOT NULL,
  birth_date TEXT NOT NULL,
  shoots TEXT NOT NULL,
  position TEXT NOT NULL,
  FOREIGN KEY (team) REFERENCES team(_id)
);

CREATE TABLE team (
  _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  founded TEXT NOT NULL,
  coach TEXT NOT NULL,
  captain INTEGER,
  won_cup INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY(captain) REFERENCES hockey_player(_id)
);

SELECT *
FROM team;

SELECT *
FROM hockey_player
JOIN team ON hockey_player.team = team._id;

SELECT *
FROM hockey_player
JOIN team ON hockey_player.team = team._id
WHERE team._id = ?;

SELECT *
FROM hockey_player
WHERE first_name = 'Alec'
UNION
SELECT cheetos.*
FROM hockey_player cheetos
WHERE first_name = 'Jake'
UNION SELECT hockey_player.*
FROM hockey_player
WHERE first_name = 'Matt';

SELECT _id
FROM (
  SELECT *
  FROM hockey_player
);

SELECT stuff._id, other_stuff.age
FROM (
  SELECT *
  FROM hockey_player
) AS stuff
JOIN hockey_player AS other_stuff;

SELECT first_name, count(*)
FROM hockey_player
GROUP BY first_name;

SELECT hockey_player.*, size
FROM hockey_player
JOIN (SELECT count(*) AS size FROM hockey_player);

SELECT *
FROM hockey_player
ORDER BY age;

SELECT _id
FROM hockey_player
WHERE ? = ?
GROUP BY ? HAVING ?
ORDER BY first_name ASC
LIMIT ?;

SELECT count(*)
FROM (
  SELECT count(*) AS cheese
  FROM hockey_player
  WHERE age = 19
) AS cheesy
WHERE cheesy.cheese = 10;

SELECT count(*)
FROM hockey_player
WHERE age=19;

SELECT *
FROM hockey_player
INNER JOIN team ON hockey_player._id = team._id;

SELECT *
FROM ( VALUES (1), (2), (3), (4) );

WITH temp_table AS (
  VALUES (1)
), temp_table2 AS (
  VALUES (1, 2)
)
SELECT *
FROM temp_table2
JOIN temp_table;

SELECT *
FROM hockey_player
WHERE _id IS NOT 2;

SELECT birth_date
FROM hockey_player
ORDER BY age
LIMIT 1;

SELECT hockey_player.*
FROM hockey_player
INNER JOIN team ON hockey_player.team = team._id;