CREATE TABLE posts (
  id TEXT NOT NULL PRIMARY KEY,
  text TEXT,
  created_at INTEGER NOT NULL
);

-- works fine.
SELECT *
FROM posts
WHERE (id, created_at) <= (?, ?)
ORDER BY created_at DESC
LIMIT 4;

-- should fail.
SELECT *
FROM posts
WHERE (id, ) <= (?, ?)