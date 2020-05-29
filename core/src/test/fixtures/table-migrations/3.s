-- error[col 12]: No table found with name new_test
INSERT INTO new_test
VALUES ("hello", "world");

ALTER TABLE test RENAME TO new_test;

INSERT INTO new_test(id, new_column)
VALUES ("hello", "world");