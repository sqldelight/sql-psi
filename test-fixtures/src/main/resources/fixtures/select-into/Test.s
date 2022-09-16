CREATE TABLE test (
  _id INTEGER NOT NULL
);

SELECT _id, _id INTO :FOO, :BAR FROM test;

-- More columns than binding, stupid, but okay
SELECT _id, _id, _id INTO :FOO, :BAR FROM test;

-- Failing, bar is not defined
SELECT _id INTO :FOO, :BAR FROM test;
