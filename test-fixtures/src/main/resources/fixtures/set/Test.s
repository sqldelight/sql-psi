CREATE TABLE hockey_player (
  _id INTEGER GENERATED ALWAYS AS (42) NOT NULL,
  first_name TEXT NOT NULL,
  last_name TEXT
);

SET :FOO = 42;
SET :FOO, :BAR = ABS(?), :A;

SET :FOO, :BAR = SELECT first_name, last_name FROM hockey_player WHERE _id = :A;

-- More columns than bindings, stupid, but okay
SET :FOO = SELECT first_name, last_name FROM hockey_player WHERE _id = :A;

-- Failing
SET :FOO, :BAR = SELECT first_name FROM hockey_player WHERE _id = :A;

-- hockey_player contains 3 columns, should work
SET :FOO, :BAR = SELECT * FROM hockey_player;
