CREATE TABLE targetActivity(
  activityId TEXT,
  targetId TEXT,
  challengeId TEXT,
  challengTarget TEXT
);

INSERT INTO targetActivity(
  activityId,
  targetId,
-- error[col 2]: <column name real> expected, got '1'
  1 /** Locally added. */
)
SELECT
  challengeId,
  targetId
  FROM challengeTarget
;