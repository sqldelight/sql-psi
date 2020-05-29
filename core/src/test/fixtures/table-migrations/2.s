ALTER TABLE test ADD COLUMN new_column TEXT NOT NULL DEFAULT 'sup';

INSERT INTO test(id, new_column)
VALUES ("hello", "world");