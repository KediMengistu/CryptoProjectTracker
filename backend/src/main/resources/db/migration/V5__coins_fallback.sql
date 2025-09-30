-- Fallback roster of coins with known GitHub repos, ordered by 'position' (1-based).
CREATE TABLE IF NOT EXISTS coins_fallback (
    coin_gecko_id TEXT PRIMARY KEY,
    symbol        TEXT,
    name          TEXT,
    repo_urls     TEXT[] NOT NULL,      -- normalized GitHub URLs
    position      INT NOT NULL,         -- 1-based ordering
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_coins_fallback_position
  ON coins_fallback (position ASC);

-- Ensure there are no empty repo sets
ALTER TABLE coins_fallback
  ADD CONSTRAINT coins_fallback_non_empty_repos
  CHECK (array_length(repo_urls, 1) IS NOT NULL AND array_length(repo_urls, 1) > 0);

CREATE UNIQUE INDEX IF NOT EXISTS uq_coins_fallback_position ON coins_fallback(position);