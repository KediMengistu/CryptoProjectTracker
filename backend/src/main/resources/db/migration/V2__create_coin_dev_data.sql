-- daily developer metrics per coin
CREATE TABLE IF NOT EXISTS coin_dev_data (
    id TEXT PRIMARY KEY,                      -- app generates (e.g., UUID string)
    coin_gecko_id TEXT NOT NULL,              -- FK to coins.coin_gecko_id
    snapshot_date DATE NOT NULL,              -- the day these numbers represent
    forks INT,
    stars INT,
    subscribers INT,
    total_issues INT,
    closed_issues INT,
    pull_requests_merged INT,
    pull_request_contributors INT,
    code_additions_4w INT,
    code_deletions_4w INT,
    commit_count_4w INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_coin_dev_data_coin
      FOREIGN KEY (coin_gecko_id) REFERENCES coins(coin_gecko_id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

-- one row per coin per day
CREATE UNIQUE INDEX IF NOT EXISTS uq_coin_dev_data_coin_day
  ON coin_dev_data (coin_gecko_id, snapshot_date);

-- speeds latest 7 days queries & pruning
CREATE INDEX IF NOT EXISTS idx_coin_dev_data_coin_day_desc
  ON coin_dev_data (coin_gecko_id, snapshot_date DESC);

-- optional: handy for “all coins for today”
-- CREATE INDEX IF NOT EXISTS idx_coin_dev_data_day ON coin_dev_data (snapshot_date);
