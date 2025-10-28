CREATE VIRTUAL TABLE data USING fts5(text, content=other_table, content_rowid=rowid, prefix='2 3 4 5 6');

SELECT rank, rowid
FROM data
WHERE text MATCH 'fts5';

SELECT rank, rowid
FROM data
WHERE text MATCH 'fts5' ORDER BY rank;

-- Expected failure - it's not valid to query for oid or docid in FTS5 tables.
SELECT oid
FROM data
WHERE text MATCH 'fts5';