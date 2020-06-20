CREATE TABLE WeatherModel (
    coordinate TEXT,
    base TEXT NOT NULL
);

CREATE TABLE Second (
    id TEXT NOT NULL
);

SELECT *, (
  SELECT wm.base
  FROM WeatherModel AS wm
  INNER JOIN Second ON cheese.base = Second.id GROUP BY Second.id
) AS field
FROM WeatherModel AS cheese;