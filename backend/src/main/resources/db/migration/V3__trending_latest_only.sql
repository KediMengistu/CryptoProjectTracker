-- Drop old uniqueness (coin_id + day) and DESC index if they exist
DROP INDEX IF EXISTS uq_coin_dev_data_coin_day;
DROP INDEX IF EXISTS idx_coin_dev_data_coin_day_desc;

-- If duplicates exist, keep only the newest (by updated_at) per coin
WITH ranked AS (
  SELECT id, coin_gecko_id,
         ROW_NUMBER() OVER (
           PARTITION BY coin_gecko_id
           ORDER BY updated_at DESC NULLS LAST, snapshot_date DESC NULLS LAST, id DESC
         ) AS rn
  FROM coin_dev_data
)
DELETE FROM coin_dev_data c
USING ranked r
WHERE c.id = r.id AND r.rn > 1;

-- Add unique constraint: exactly one dev row per coin
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'uq_coin_dev_data_coin_unique'
  ) THEN
    ALTER TABLE coin_dev_data
      ADD CONSTRAINT uq_coin_dev_data_coin_unique UNIQUE (coin_gecko_id);
  END IF;
END$$;

-- Ensure FK is present and cascading (idempotent-ish)
ALTER TABLE coin_dev_data
  DROP CONSTRAINT IF EXISTS fk_coin_dev_data_coin,
  ADD CONSTRAINT fk_coin_dev_data_coin
    FOREIGN KEY (coin_gecko_id) REFERENCES coins(coin_gecko_id)
    ON UPDATE CASCADE ON DELETE CASCADE;

-- Helpful index for lookups by coin
CREATE INDEX IF NOT EXISTS idx_coin_dev_data_coin ON coin_dev_data (coin_gecko_id);
