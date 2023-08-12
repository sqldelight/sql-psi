CREATE TABLE test (
  column1 TEXT NOT NULL,
  column2 TEXT NOT NULL
);

SELECT column2
FROM test
ORDER BY column1;

CREATE TABLE checkin (
  id INTEGER NOT NULL,
  created_at TEXT NOT NULL,
  user_id INTEGER NOT NULL
);

CREATE TABLE user (
  id INTEGER NOT NULL,
  first_name TEXT,
  last_name TEXT
);

SELECT
  checkin.id,
  checkin.created_at,
  user.id AS user_id,
  user.first_name,
  user.last_name
FROM checkin
  JOIN user ON checkin.user_id = user.id
ORDER BY id DESC;