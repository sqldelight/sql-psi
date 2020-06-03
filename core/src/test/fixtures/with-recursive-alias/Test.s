CREATE TABLE IF NOT EXISTS mp_screen_stack (
    screen_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    parent_id INTEGER,
    screen_code TEXT NOT NULL
);

WITH RECURSIVE bottom_top AS (
     SELECT
       screen_id,
       parent_id
     FROM mp_screen_stack
     WHERE screen_id = ?
   UNION ALL
     SELECT
       parent.screen_id,
       parent.parent_id
     FROM bottom_top AS child
     INNER JOIN mp_screen_stack    AS parent
     ON child.parent_id = parent.screen_id
)
SELECT * FROM bottom_top;