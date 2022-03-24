CREATE TABLE IF NOT EXISTS task (id TEXT NOT NULL, title TEXT NOT NULL, description TEXT, completed INTEGER NOT NULL, create_time INTEGER NOT NULL, update_time INTEGER NOT NULL, PRIMARY KEY(id));
CREATE  INDEX index_task_completed_create_time ON task (completed, create_time);
CREATE  INDEX index_task_title ON task (title);
CREATE  INDEX index_task_create_time_completed_id_title ON task (create_time, completed, id, title);
CREATE VIRTUAL TABLE IF NOT EXISTS task_fts USING FTS4(id TEXT NOT NULL, title TEXT NOT NULL, description TEXT, completed INTEGER NOT NULL, create_time INTEGER NOT NULL, update_time INTEGER NOT NULL, content=task);
CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_task_fts_BEFORE_UPDATE BEFORE UPDATE ON task BEGIN DELETE FROM task_fts WHERE docid=old.rowid; END;
CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_task_fts_BEFORE_DELETE BEFORE DELETE ON task BEGIN DELETE FROM task_fts WHERE docid=old.rowid; END;
CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_task_fts_AFTER_UPDATE AFTER UPDATE ON task BEGIN INSERT INTO task_fts(docid, id, title, description, completed, create_time, update_time) VALUES (new.rowid, new.id, new.title, new.description, new.completed, new.create_time, new.update_time); END;
CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_task_fts_AFTER_INSERT AFTER INSERT ON task BEGIN INSERT INTO task_fts(docid, id, title, description, completed, create_time, update_time) VALUES (new.rowid, new.id, new.title, new.description, new.completed, new.create_time, new.update_time); END;