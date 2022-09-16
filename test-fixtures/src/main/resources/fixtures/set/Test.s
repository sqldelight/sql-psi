CREATE TABLE hockey_player (
  _id INTEGER NOT NULL,
  first_name TEXT NOT NULL,
  last_name TEXT
);

SET :FOO = 42;
SET :FOO, :BAR = ABS(42), :A;

SET :FOO, :BAR = SELECT first_name, last_name FROM hockey_player WHERE _id = :A;

-- More columns than binding, stupid, but okay
SET :FOO = SELECT first_name, last_name FROM hockey_player WHERE _id = :A;

-- Failing
SET :FOO, :BAR = SELECT first_name FROM hockey_player WHERE _id = :A;
