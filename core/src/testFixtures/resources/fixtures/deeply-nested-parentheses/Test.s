CREATE TABLE t (
  a INTEGER NOT NULL,
  b INTEGER NOT NULL
);

SELECT *
FROM t
WHERE (((((((((((((((((((((((((a = 1)))))))))))))))))))))))));

SELECT *
FROM t
WHERE (a = 1 OR (a = 2 OR (a = 3 OR (a = 4 OR (a = 5 OR (a = 6 OR (a = 7 OR (a = 8 OR (a = 9 OR (a = 10 OR (a = 11 OR (a = 12 OR (a = 13 OR (a = 14 OR (a = 15 OR (a = 16 OR (a = 17 OR (a = 18 OR (a = 19 OR (a = 20 OR (a = 21 OR (a = 22 OR (a = 23 OR (a = 24 OR (a = 25)))))))))))))))))))))))));

SELECT *
FROM t
WHERE ((((((((((((((((((((((((((a, b) IN (SELECT a, b FROM t))))))))))))))))))))))))));
