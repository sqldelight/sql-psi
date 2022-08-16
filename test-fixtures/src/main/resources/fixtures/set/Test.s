CREATE TABLE hockey_player (
  _id INTEGER NOT NULL,
  first_name TEXT NOT NULL,
  last_name TEXT
);

SET ? = 42;
SET ?, ? = ABS(42), 42;

SET ?, ? = SELECT first_name, last_name FROM hockey_player WHERE _id = ?;
