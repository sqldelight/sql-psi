CREATE VIRTUAL TABLE data5 USING fts5(text, content=other_table, content_rowid=rowid, prefix='2 3 4 5 6');

SELECT *
FROM data5
WHERE text MATCH 'fts5';

SELECT text
FROM data5
WHERE text MATCH 'fts5';

INSERT INTO data5(text)
VALUES (?);

INSERT INTO data5
VALUES (?);
