CREATE TABLE test(
  someColumn TEXT NOT NULL
);

SELECT someColumn
FROM test
GROUP BY COALESCE(someColumn, 'default');
