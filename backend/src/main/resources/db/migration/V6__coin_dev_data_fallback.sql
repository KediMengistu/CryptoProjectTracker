-- Latest-only developer snapshot for each fallback coin.
CREATE TABLE IF NOT EXISTS coin_dev_data_fallback (
    id                          TEXT PRIMARY KEY,
    coin_gecko_id               TEXT NOT NULL UNIQUE,
    snapshot_date               DATE NOT NULL,
    forks                       INT,
    stars                       INT,
    subscribers                 INT,
    total_issues                INT,
    closed_issues               INT,
    pull_requests_merged        INT,
    pull_request_contributors   INT,
    code_additions_4w           INT,
    code_deletions_4w           INT,
    commit_count_4w             INT,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cdd_fallback_coin
      FOREIGN KEY (coin_gecko_id) REFERENCES coins_fallback(coin_gecko_id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

-- If V4 added DB-generated IDs, reuse the same mechanism (TEXT ids backed by UUID default)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
ALTER TABLE coin_dev_data_fallback
  ALTER COLUMN id SET DEFAULT gen_random_uuid()::text;

CREATE INDEX IF NOT EXISTS idx_cdd_fallback_coin ON coin_dev_data_fallback (coin_gecko_id);
