CREATE TABLE task
  (
     id          TEXT NOT NULL,
     title       TEXT NOT NULL,
     description TEXT,
     completed   INTEGER NOT NULL,
     PRIMARY KEY(id)
  );

CREATE VIRTUAL TABLE task_fts USING FTS4(title TEXT NOT NULL, description TEXT, content=task);

CREATE TRIGGER content_sync_task_fts_BEFORE_UPDATE
BEFORE UPDATE ON task
BEGIN
    DELETE
    FROM task_fts
    WHERE docid=old.rowid;
END;