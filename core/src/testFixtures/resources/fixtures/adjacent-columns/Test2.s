CREATE TABLE coachee (
  id TEXT NOT NULL PRIMARY KEY
);

CREATE TABLE chat (
  id TEXT NOT NULL PRIMARY KEY,
  coacheeId TEXT NOT NULL
);

CREATE TABLE coachTagCoachee (
  coachTagId TEXT NOT NULL,
  coacheeId TEXT NOT NULL,
  PRIMARY KEY (coachTagId, coacheeId)
);

CREATE TABLE coachTag (
  id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL
);

SELECT
  coachTagCoachee.coachTagId AS coachTagId,
  coachTag.name AS coachTagName,
  COUNT(coachTag.id) AS numberOfCoachees
  FROM chat
  JOIN coachee
    ON coachee.id = chat.coacheeId
  LEFT JOIN coachTagCoachee
    ON coachTagCoachee.coacheeId = coachee.id
  LEFT JOIN coachTag
    ON coachTag.id = coachTagCoachee.coachTagId
  GROUP BY coachTagId
  ORDER BY coachTagName IS NULL, coachTagName COLLATE NOCASE ASC, coachTagId ASC
;
