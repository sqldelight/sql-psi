CREATE TABLE test(
  value TEXT
);

CREATE TABLE IF NOT EXISTS test(
  value TEXT
);

-- error[col 13]: Table already defined with name test
CREATE TABLE test(
  value TEXT
);

SELECT value
FROM test;

CREATE VIEW testView AS
SELECT value, value AS value2
FROM test;

CREATE VIEW IF NOT EXISTS testView AS
SELECT value, value AS value2
FROM test;

-- error[col 12]: Table already defined with name testView
CREATE VIEW testView AS
SELECT value, value AS value2
FROM test;

SELECT value, value2
FROM testView;

CREATE INDEX testIndex ON test(value);
CREATE INDEX IF NOT EXISTS testIndex ON test(value);
-- error[col 13]: Duplicate index name testIndex
CREATE INDEX testIndex ON test(value);

CREATE TRIGGER testTrigger
BEFORE INSERT ON test
BEGIN
INSERT INTO test VALUES ('sup');
END;

CREATE TRIGGER IF NOT EXISTS testTrigger
BEFORE INSERT ON test
BEGIN
INSERT INTO test VALUES ('sup');
END;

-- error[col 15]: Duplicate trigger name testTrigger
CREATE TRIGGER testTrigger
BEFORE INSERT ON test
BEGIN
INSERT INTO test VALUES ('sup');
END;