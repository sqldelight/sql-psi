DROP TRIGGER cheese_trigger;

CREATE TRIGGER cheese_trigger AFTER UPDATE OF cheese ON food
BEGIN
DELETE FROM food;
END;