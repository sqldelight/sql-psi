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
  position TEXT NOT NULL
);

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