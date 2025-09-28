-- coins table
CREATE TABLE IF NOT EXISTS coins (
    id TEXT PRIMARY KEY,                    -- app generates (e.g., UUID string)
    coin_gecko_id TEXT NOT NULL UNIQUE,     -- unique coin key from CoinGecko
    symbol TEXT,
    name TEXT,
    coin_gecko_repo_url_github TEXT[],      -- Postgres text[]
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- functional index for case-insensitive symbol lookups
CREATE INDEX IF NOT EXISTS idx_coins_lower_symbol ON coins (LOWER(symbol));
