CREATE TABLE transaction(
    transaction_id INTEGER PRIMARY KEY,
    transaction_uuid TEXT NOT NULL UNIQUE,
    opened_time TEXT NOT NULL,
    finalized_time TEXT
);

INSERT INTO transaction(transaction_uuid, opened_time, finalized_time)
VALUES (?, ?, ?)
ON CONFLICT (transaction_uuid)
DO UPDATE SET opened_time = excluded.opened_time, finalized_time = excluded.finalized_time;

INSERT INTO transaction(transaction_uuid, opened_time, finalized_time)
VALUES (?, ?, ?)
ON CONFLICT (transaction_uuid)
DO UPDATE SET finalized_time = excluded.finalized_time
WHERE finalized_time IS NULL;

INSERT INTO transaction(transaction_uuid, opened_time, finalized_time)
VALUES (?, ?, ?)
ON CONFLICT (transaction_uuid) DO NOTHING;

INSERT INTO transaction(transaction_uuid, opened_time, finalized_time)
VALUES (?, ?, ?)
ON CONFLICT DO NOTHING;

-- SET clause should be able to access CTE
WITH t(foo) AS (VALUES (?))
INSERT INTO transaction(transaction_uuid, opened_time, finalized_time)
VALUES (?, ?, ?)
ON CONFLICT (transaction_uuid)
DO UPDATE SET opened_time = excluded.opened_time, finalized_time = (SELECT foo FROM t);

-- Conflict list should only be able to access the transaction table, not the CTE
WITH t(foo) AS (VALUES(?))
INSERT INTO transaction(transaction_uuid, opened_time, finalized_time)
VALUES (?, ?, ?)
ON CONFLICT (foo) DO NOTHING;

-- If specifying a conflict resolution strategy like REPLACE, the ON CONFLICT must be DO NOTHING
INSERT OR REPLACE INTO transaction(transaction_uuid, opened_time, finalized_time)
VALUES (?, ?, ?)
ON CONFLICT (transaction_uuid) DO NOTHING;

-- Cannot use DO UPDATE when OR REPLACE conflict resolution algorithm was specified
INSERT OR REPLACE INTO transaction(transaction_uuid, opened_time, finalized_time)
VALUES (?, ?, ?)
ON CONFLICT (transaction_uuid) DO UPDATE SET opened_time = excluded.opened_time;
