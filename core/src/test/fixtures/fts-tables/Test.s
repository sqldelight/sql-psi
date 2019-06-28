CREATE VIRTUAL TABLE data USING fts3(content TEXT, content2 TEXT);

INSERT INTO data(content, content2)
VALUES (?, ?);

INSERT INTO data(docid, content, content2)
VALUES (?, ?, ?);

INSERT INTO data
VALUES (?, ?);

SELECT *
FROM data
WHERE content LIKE '%' | ? | '%';

SELECT docid
FROM data;

SELECT *
FROM data
WHERE data MATCH ?;

SELECT *
FROM data
WHERE data.data MATCH ?;

SELECT *
FROM data
WHERE main.data.data MATCH ?
ORDER BY rank(matchinfo(data));

-- Expected failure - data is not a valid column in this context
SELECT *
FROM data
WHERE main.data MATCH ?;
