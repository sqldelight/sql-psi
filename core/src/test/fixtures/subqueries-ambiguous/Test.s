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
FROM hockey_player AS one
JOIN (
  SELECT *
  FROM (
    SELECT *
    FROM (
      SELECT *
      FROM hockey_player
    )
  )
)
GROUP BY team;