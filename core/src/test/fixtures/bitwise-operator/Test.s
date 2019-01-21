CREATE TABLE message (
  messageId INTEGER NOT NULL,
  bufferId INTEGER NOT NULL,
  flags INTEGER NOT NULL
);

SELECT messageId
FROM message
WHERE bufferId = :bufferId
AND flags & ~:ignored > 0
ORDER BY messageId ASC
LIMIT 1;