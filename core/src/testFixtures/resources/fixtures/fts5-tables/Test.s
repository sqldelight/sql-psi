CREATE VIRTUAL TABLE ft_locale USING fts5(a, b, c, locale=1);
CREATE VIRTUAL TABLE ft_columnsize USING fts5(a, b, c, columnsize=0);
CREATE VIRTUAL TABLE ft_contentless_delete USING fts5(a, b, c, content='', contentless_delete=1);
CREATE VIRTUAL TABLE ft_contentless_unindexed USING fts5(a, b UNINDEXED, content='', contentless_unindexed=1);

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
