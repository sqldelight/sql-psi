CREATE TABLE parent (id INTEGER PRIMARY KEY);
CREATE TABLE child (parent_id INTEGER NOT NULL, value TEXT NOT NULL);
CREATE TABLE summary (parent_id INTEGER NOT NULL, max_value TEXT);

INSERT INTO summary (parent_id, max_value)
SELECT p.id, (SELECT max(c.value) FROM child c WHERE c.parent_id = p.id)
FROM parent p;

INSERT INTO summary (parent_id, max_value)
SELECT p.id, (SELECT max(c.value) FROM child c WHERE c.parent_id = p.id)
FROM parent p
WHERE EXISTS (SELECT 1 FROM child c WHERE c.parent_id = p.id);

INSERT INTO summary (parent_id, max_value)
SELECT p.id, sub.max_value
FROM parent p
JOIN (SELECT c.parent_id, max(c.value) AS max_value FROM child c GROUP BY c.parent_id) sub
  ON sub.parent_id = p.id;
