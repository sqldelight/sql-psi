CREATE TABLE test(
  id INTEGER NOT NULL
);

WITH RECURSIVE stuff(thing) AS (
  SELECT 1
)
SELECT *
FROM test
JOIN stuff
WHERE id IN (
  WITH RECURSIVE inner(token, str) AS (
    SELECT '', thing || ''
    UNION ALL SELECT
      substr(str, 0, instr(str, ',')),
      substr(str, instr(str, ',') + 1)
    FROM inner WHERE str != ''
  )
  SELECT token
  FROM inner
);