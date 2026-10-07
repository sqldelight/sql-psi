CREATE TABLE foo(
    id TEXT NOT NULL PRIMARY KEY,
    /** This is a jdoc comment, not a block comment */
    value INTEGER NOT NULL,
    /* This one is trivia, and is dropped. */
    baz TEXT /* mid-definition */ NOT NULL
);
