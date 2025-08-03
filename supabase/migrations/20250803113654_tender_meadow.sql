/*
# Fix JSONB Column Type Mismatch

This migration converts JSONB columns to TEXT to match Hibernate's VARCHAR mapping
and avoid type casting issues.

1. Changes
   - Convert strategies.config_json from JSONB to TEXT
   - Convert strategy_versions.config_json from JSONB to TEXT  
   - Convert strategy_signals.indicator_values from JSONB to TEXT

2. Benefits
   - Eliminates PostgreSQL type casting errors
   - Maintains JSON functionality through application parsing
   - Compatible with Hibernate's default string mapping
*/

-- Convert strategies.config_json from JSONB to TEXT
ALTER TABLE strategies ALTER COLUMN config_json TYPE TEXT USING config_json::TEXT;

-- Convert strategy_versions.config_json from JSONB to TEXT  
ALTER TABLE strategy_versions ALTER COLUMN config_json TYPE TEXT USING config_json::TEXT;

-- Convert strategy_signals.indicator_values from JSONB to TEXT
ALTER TABLE strategy_signals ALTER COLUMN indicator_values TYPE TEXT USING indicator_values::TEXT;

-- Add comments to document the change
COMMENT ON COLUMN strategies.config_json IS 'Strategy configuration stored as JSON text';
COMMENT ON COLUMN strategy_versions.config_json IS 'Strategy version configuration stored as JSON text';
COMMENT ON COLUMN strategy_signals.indicator_values IS 'Indicator values stored as JSON text';