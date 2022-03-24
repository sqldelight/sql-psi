CREATE TABLE test (
  _id INTEGER PRIMARY KEY AUTOINCREMENT
);

SELECT *, CASE 0 WHEN 0 THEN coalesce(_id, test.fake_column) ELSE 'sup' END
FROM test;

SELECT *, CASE 0 WHEN 0 THEN coalesce(test.fake_column, _id) ELSE 'sup' END
FROM test;