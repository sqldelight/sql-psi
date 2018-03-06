SELECT datetime('now') AS date
ORDER BY date;

CREATE TABLE test(
  stuff INTEGER NOT NULL,
  date TEXT NOT NULL
);

SELECT stuff
FROM test
ORDER BY stuff;

SELECT stuff
FROM test
ORDER BY date;