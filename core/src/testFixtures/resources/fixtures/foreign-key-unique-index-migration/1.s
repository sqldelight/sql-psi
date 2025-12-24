CREATE TABLE table_a(
    id INTEGER PRIMARY KEY
);

CREATE TABLE table_b(
    id   INTEGER PRIMARY KEY,
    a_id INTEGER NOT NULL REFERENCES table_a (id)
);

CREATE UNIQUE INDEX uk_table_b_a_id_id ON table_b (a_id, id);

-- test that unique index can be validated in same migration file
CREATE TABLE table_c (
    id   INTEGER PRIMARY KEY,
    a_id INTEGER NOT NULL REFERENCES table_a (id),
    b_id INTEGER DEFAULT NULL,
    FOREIGN KEY (a_id, b_id) REFERENCES table_b (a_id, id)
);
