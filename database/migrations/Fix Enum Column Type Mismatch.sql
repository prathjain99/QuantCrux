/*
# Fix Enum Column Type Mismatch

This migration converts enum columns to VARCHAR to match Hibernate's string mapping
and avoid type casting issues with PostgreSQL custom enum types.

1. Changes
   - Convert strategies.status from strategy_status enum to VARCHAR
   - Convert strategy_signals.signal_type from signal_type enum to VARCHAR
   - Convert users.role from user_role enum to VARCHAR (if needed)
   - Convert users.account_status from account_status enum to VARCHAR (if needed)
   - Add CHECK constraints to maintain data integrity

2. Benefits
   - Eliminates PostgreSQL enum casting errors
   - Maintains data validation through CHECK constraints
   - Compatible with Hibernate's default enum handling
*/

-- Convert strategies.status from enum to VARCHAR
ALTER TABLE strategies ALTER COLUMN status TYPE VARCHAR(20) USING status::TEXT;

-- Add CHECK constraint to maintain data integrity for strategy status
ALTER TABLE strategies DROP CONSTRAINT IF EXISTS valid_status;
ALTER TABLE strategies ADD CONSTRAINT valid_status CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'ARCHIVED'));

-- Convert strategy_signals.signal_type from enum to VARCHAR
ALTER TABLE strategy_signals ALTER COLUMN signal_type TYPE VARCHAR(20) USING signal_type::TEXT;

-- Add CHECK constraint to maintain data integrity for signal type
ALTER TABLE strategy_signals DROP CONSTRAINT IF EXISTS valid_signal_type;
ALTER TABLE strategy_signals ADD CONSTRAINT valid_signal_type CHECK (signal_type IN ('BUY', 'SELL', 'HOLD', 'NO_SIGNAL'));

-- Convert users.role from enum to VARCHAR (if it exists as enum)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'role' AND data_type = 'USER-DEFINED') THEN
        ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(50) USING role::TEXT;
        ALTER TABLE users DROP CONSTRAINT IF EXISTS valid_role;
        ALTER TABLE users ADD CONSTRAINT valid_role CHECK (role IN ('CLIENT', 'PORTFOLIO_MANAGER', 'RESEARCHER', 'ADMIN'));
    END IF;
END $$;

-- Convert users.account_status from enum to VARCHAR (if it exists as enum)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'account_status' AND data_type = 'USER-DEFINED') THEN
        ALTER TABLE users ALTER COLUMN account_status TYPE VARCHAR(50) USING account_status::TEXT;
        ALTER TABLE users DROP CONSTRAINT IF EXISTS valid_account_status;
        ALTER TABLE users ADD CONSTRAINT valid_account_status CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION'));
    END IF;
END $$;

-- Drop the custom enum types if they exist and are no longer used
DROP TYPE IF EXISTS strategy_status CASCADE;
DROP TYPE IF EXISTS signal_type CASCADE;

-- Add comments to document the change
COMMENT ON COLUMN strategies.status IS 'Strategy status stored as VARCHAR with CHECK constraint';
COMMENT ON COLUMN strategy_signals.signal_type IS 'Signal type stored as VARCHAR with CHECK constraint';

-- Update any existing data to ensure consistency
UPDATE strategies SET status = 'DRAFT' WHERE status IS NULL;
UPDATE strategy_signals SET signal_type = 'NO_SIGNAL' WHERE signal_type IS NULL;