CREATE TABLE my_table (
  column_a TEXT
);

CREATE TRIGGER unique_column_a_before_insert_my_table BEFORE INSERT ON my_table
BEGIN SELECT CASE
  WHEN (new.column_a != NULL AND EXISTS(SELECT 1 FROM my_table WHERE my_table.column_a = new.column_a)) THEN
    RAISE(ABORT, 'Column A already exists')
  END;
END;