-- error[col 48]: No table found with name Channel
CREATE UNIQUE INDEX idx_channel_original_url ON Channel(
-- error[col 2]: No column found with name originalUrl
  originalUrl
);