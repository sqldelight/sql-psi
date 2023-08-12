-- Table definition edited down to the relevant columns:
CREATE TABLE `models` (
  `model_id` TEXT NOT NULL,
  `model_descriptor_id` TEXT NOT NULL,
  `build_date` TEXT NOT NULL,
  PRIMARY KEY (`model_id`)
);

SELECT max(build_date) AS build_date_seconds
FROM models
GROUP BY model_descriptor_id
HAVING build_date_seconds > 0 LIMIT 1;