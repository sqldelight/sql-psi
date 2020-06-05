CREATE TABLE test (
  id TEXT NOT NULL
);

-- error[col 0]: Alter table statements are forbidden outside of migration files.
ALTER TABLE test
  ADD COLUMN id2 TEXT;