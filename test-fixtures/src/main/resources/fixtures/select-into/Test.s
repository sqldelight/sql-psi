CREATE TABLE test (
  id INTEGER GENERATED ALWAYS AS (42) NOT NULL,
  bar INTEGER NOT NULL
);

SELECT id, bar INTO :FOO, :BAR FROM test;

-- More columns than bindings, stupid, but okay
SELECT id, bar, bar INTO :FOO, :BAR FROM test;

-- Failing, bar is not defined
SELECT id INTO :FOO, :BAR FROM test;

-- test contains 2 columns, should work
SELECT * INTO :FOO, :BAR FROM test;
