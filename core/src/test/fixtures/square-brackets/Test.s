CREATE TABLE book(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title TEXT
);

INSERT INTO book(title)
VALUES
  ('Alice in Wonderland'),
  ('Where the Sidewalk Ends'),
  ('The Cat in the Hat'),
  ('Charlie and the Chocolate Factory');

SELECT title FROM book ORDER BY [id] % 2, id;