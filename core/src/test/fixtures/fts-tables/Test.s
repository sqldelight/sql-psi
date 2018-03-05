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
