CREATE TABLE game(
  id INTEGER
);

-- error[col 6]: FROM expected, got '*'
DELETE * FROM game;