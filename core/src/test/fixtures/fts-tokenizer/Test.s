CREATE VIRTUAL TABLE entity_fts USING fts4 (
  tokenize=simple X "$ *&#%\'""\/(){}\[]|=+-_,:;<>-?!\t\r\n",
  text_content TEXT
);

DELETE FROM entity_fts;

INSERT INTO entity_fts (text_content)
VALUES (?);