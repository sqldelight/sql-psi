CREATE TABLE entries (
  category TEXT
);

SELECT category, COUNT(category)
FROM entries
GROUP BY category
ORDER BY COUNT(category) DESC;