CREATE TABLE Test1(id TEXT);
CREATE TABLE Test2(id TEXT);

SELECT t1.*
FROM Test1 t1
WHERE t1.rowid IS NOT NULL
ORDER BY t1.rowid ASC
LIMIT 1;

SELECT t1.*, t2.* FROM Test1 t1
JOIN Test2 t2 ON t1.id = t2.id
WHERE t1.id = 'something good'
ORDER BY t1.rowid ASC
LIMIT 1;