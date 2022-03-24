CREATE TABLE food(
  cheese TEXT NOT NULL
);

-- error[col 13]: No trigger found with name cheese_trigger
DROP TRIGGER cheese_trigger;

CREATE TRIGGER cheese_trigger AFTER UPDATE OF cheese ON food
BEGIN
DELETE FROM food;
END;

DROP TRIGGER cheese_trigger;