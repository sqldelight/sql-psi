CREATE TABLE foo(
    id TEXT NOT NULL PRIMARY KEY
);

CREATE TABLE bar(
    id TEXT NOT NULL PRIMARY KEY
);

CREATE TABLE baz(
    id TEXT NOT NULL PRIMARY KEY
);

SELECT id FROM foo
WHERE id IN (
    SELECT foo.id FROM foo
    LEFT JOIN bar ON foo.id = bar.id
    LEFT JOIN baz ON foo.id = baz.id
);