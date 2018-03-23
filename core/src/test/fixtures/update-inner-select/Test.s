CREATE TABLE message (
    mid INTEGER PRIMARY KEY NOT NULL,
    fid INTEGER NOT NULL
);

CREATE TABLE folder (
    fid INTEGER PRIMARY KEY NOT NULL,
    total_counter INTEGER NOT NULL
);

UPDATE folder SET
total_counter = (SELECT COUNT(*) FROM message WHERE folder.fid=message.fid)
WHERE folder.fid = ?;
