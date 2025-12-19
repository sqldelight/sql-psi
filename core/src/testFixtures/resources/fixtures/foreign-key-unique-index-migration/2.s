-- test that unique index can be validated across migration files
CREATE TABLE table_d (
    id   INTEGER PRIMARY KEY,
    a_id INTEGER NOT NULL REFERENCES table_a (id),
    b_id INTEGER DEFAULT NULL,
    FOREIGN KEY (a_id, b_id) REFERENCES table_b (a_id, id)
);

