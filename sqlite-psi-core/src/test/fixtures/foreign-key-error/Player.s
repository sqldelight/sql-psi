CREATE TABLE player (
  name TEXT NOT NULL,
  number INTEGER NOT NULL,
  team TEXT REFERENCES team(name),
  PRIMARY KEY (team, number)
);