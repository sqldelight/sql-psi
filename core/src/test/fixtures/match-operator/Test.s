CREATE TABLE book(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  author TEXT NOT NULL,
  publisher TEXT NOT NULL
);

CREATE VIRTUAL TABLE book_fts USING fts4(content TEXT);

-- Expected failure: MATCH can't be used on a regular table
SELECT * FROM book WHERE book MATCH ?;

-- Expected failure: MATCH can't be used on a column within a regular table
SELECT * FROM book WHERE title MATCH ?;

SELECT * FROM book_fts WHERE content MATCH ?;

SELECT book.*
FROM book
INNER JOIN book_fts ON docid = book.id
WHERE book_fts MATCH ?1;

SELECT book.*
FROM book_fts
LEFT JOIN book ON docid = book.id
WHERE book_fts MATCH ?1;

SELECT book.*
FROM book_fts
LEFT JOIN book ON docid = book.id
WHERE content MATCH ?1;

-- Expected failure: MATCH can't be used when an FTS table is on the right of a left join
SELECT book.*
FROM book
LEFT JOIN book_fts ON docid = book.id
WHERE book_fts MATCH ?1;

-- Expected failure: MATCH can't be used when an FTS table is on the right of a left join
SELECT book.*
FROM book
LEFT JOIN book_fts ON docid = book.id
WHERE content MATCH ?1;

-- Expected failure: MATCH can't be used on a regular table
SELECT book.*
FROM book_fts
JOIN book ON docid = book.id
WHERE title MATCH ?1;

SELECT book.*
FROM book_fts
JOIN book ON docid = book.id
WHERE book_fts MATCH title;

-- Expected failure: Cannot bind both sides of a MATCH expression
SELECT book.*
FROM book_fts
JOIN book ON docid = book.id
WHERE :bind MATCH :searchText;

-- Expected failure: MATCH can't be used on a literal expression
SELECT book.*
FROM book_fts
JOIN book ON docid = book.id
WHERE 'literal' MATCH :searchText;

-- Expected failure: MATCH can't be used on a CASE statement
SELECT book.*
FROM book_fts
JOIN book ON docid = book.id
WHERE (CASE WHEN book.id = 1 THEN book_fts ELSE book_fts.content END) MATCH :searchText;
