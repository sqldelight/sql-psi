CREATE TABLE destination (
  destination_id INTEGER PRIMARY KEY,
  t TEXT NOT NULL
);

CREATE TABLE source (
  source_id INTEGER PRIMARY KEY,
  t TEXT NOT NULL
);

INSERT INTO destination (destination_id, t)
SELECT destination_id, t FROM source;

INSERT INTO destination (destination_id, t)
SELECT source_id, t FROM source
WHERE destination_id > 0;
