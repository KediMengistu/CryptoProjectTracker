-- V4__db_generated_ids.sql
-- Let Postgres generate primary keys, so the app can insert with id = NULL.

-- Enable UUID generation (Supabase has pgcrypto available)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- coins.id gets a DB default (we keep TEXT type and cast the UUID to text)
ALTER TABLE coins
  ALTER COLUMN id SET DEFAULT gen_random_uuid()::text;

-- coin_dev_data.id gets a DB default as well
ALTER TABLE coin_dev_data
  ALTER COLUMN id SET DEFAULT gen_random_uuid()::text;
