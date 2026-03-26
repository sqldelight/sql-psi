CREATE TABLE destination (
  name TEXT
);

CREATE TABLE source (
  name TEXT NOT NULL
);

INSERT INTO destination (name)
SELECT source.name
FROM source
LEFT JOIN destination ON source.name = destination.name
WHERE destination.name IS NULL;

