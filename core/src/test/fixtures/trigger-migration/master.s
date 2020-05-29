-- error[col 15]: Duplicate trigger name cheese_trigger
CREATE TRIGGER cheese_trigger AFTER UPDATE OF cheese ON food
BEGIN
DELETE FROM food;
END;