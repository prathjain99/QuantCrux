/*
# QuantCrux Strategy Builder Schema - Simplified Version

1. New Tables
   - `strategies` - Core strategy definitions with TEXT config (will be JSON strings)
   - `strategy_versions` - Version control for strategy changes
   - `strategy_tags` - Strategy tags (normalized)
   - `strategy_signals` - Historical signal logs
   - `signal_matched_rules` - Signal matched rules (normalized)

2. Security
   - Enable RLS on all tables
   - Role-based policies for RESEARCHER and PORTFOLIO_MANAGER access
   - Proper indexes for performance

3. Features
   - TEXT storage for JSON configuration (avoids JSONB casting issues)
   - Version control with rollback capability
   - Signal history tracking
*/

-- Strategies table (using TEXT for JSON to avoid casting issues)
CREATE TABLE IF NOT EXISTS strategies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    symbol VARCHAR(20) NOT NULL,
    timeframe VARCHAR(10) NOT NULL DEFAULT '1m',
    config_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    current_version INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_timeframe CHECK (timeframe IN ('1m', '5m', '15m', '30m', '1h', '4h', '1d')),
    CONSTRAINT valid_status CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'ARCHIVED'))
);

-- Strategy tags table (normalized)
CREATE TABLE IF NOT EXISTS strategy_tags (
    strategy_id UUID NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (strategy_id, tag)
);

-- Strategy versions for version control
CREATE TABLE IF NOT EXISTS strategy_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_id UUID NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    config_json TEXT NOT NULL,
    change_description TEXT,
    author_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(strategy_id, version_number)
);

-- Strategy signals log
CREATE TABLE IF NOT EXISTS strategy_signals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_id UUID NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    signal_type VARCHAR(20) NOT NULL,
    price DECIMAL(15,6),
    indicator_values TEXT,
    confidence_score DECIMAL(5,4),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_signal_type CHECK (signal_type IN ('BUY', 'SELL', 'HOLD', 'NO_SIGNAL'))
);

-- Signal matched rules table (normalized)
CREATE TABLE IF NOT EXISTS signal_matched_rules (
    signal_id UUID NOT NULL REFERENCES strategy_signals(id) ON DELETE CASCADE,
    matched_rule VARCHAR(255) NOT NULL,
    PRIMARY KEY (signal_id, matched_rule)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_strategies_user_id ON strategies(user_id);
CREATE INDEX IF NOT EXISTS idx_strategies_symbol ON strategies(symbol);
CREATE INDEX IF NOT EXISTS idx_strategies_status ON strategies(status);
CREATE INDEX IF NOT EXISTS idx_strategy_tags_strategy_id ON strategy_tags(strategy_id);
CREATE INDEX IF NOT EXISTS idx_strategy_tags_tag ON strategy_tags(tag);
CREATE INDEX IF NOT EXISTS idx_strategy_versions_strategy_id ON strategy_versions(strategy_id);
CREATE INDEX IF NOT EXISTS idx_strategy_signals_strategy_id ON strategy_signals(strategy_id);
CREATE INDEX IF NOT EXISTS idx_strategy_signals_created_at ON strategy_signals(created_at);

-- Enable Row Level Security
ALTER TABLE strategies ENABLE ROW LEVEL SECURITY;
ALTER TABLE strategy_tags ENABLE ROW LEVEL SECURITY;
ALTER TABLE strategy_versions ENABLE ROW LEVEL SECURITY;
ALTER TABLE strategy_signals ENABLE ROW LEVEL SECURITY;
ALTER TABLE signal_matched_rules ENABLE ROW LEVEL SECURITY;

-- RLS Policies for strategies
CREATE POLICY "Users can read own strategies" ON strategies
    FOR SELECT USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can create strategies" ON strategies
    FOR INSERT WITH CHECK (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can update own strategies" ON strategies
    FOR UPDATE USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can delete own strategies" ON strategies
    FOR DELETE USING (user_id = current_setting('app.current_user_id', true)::UUID);

-- RLS Policies for strategy_tags
CREATE POLICY "Users can read own strategy tags" ON strategy_tags
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM strategies s 
            WHERE s.id = strategy_tags.strategy_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "Users can manage own strategy tags" ON strategy_tags
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM strategies s 
            WHERE s.id = strategy_tags.strategy_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

-- RLS Policies for strategy_versions
CREATE POLICY "Users can read own strategy versions" ON strategy_versions
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM strategies s 
            WHERE s.id = strategy_versions.strategy_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "Users can create strategy versions" ON strategy_versions
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM strategies s 
            WHERE s.id = strategy_versions.strategy_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

-- RLS Policies for strategy_signals
CREATE POLICY "Users can read own strategy signals" ON strategy_signals
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM strategies s 
            WHERE s.id = strategy_signals.strategy_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

-- RLS Policies for signal_matched_rules
CREATE POLICY "Users can read own signal rules" ON signal_matched_rules
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM strategy_signals ss
            JOIN strategies s ON s.id = ss.strategy_id
            WHERE ss.id = signal_matched_rules.signal_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

-- Update timestamp trigger for strategies
CREATE TRIGGER update_strategies_updated_at BEFORE UPDATE ON strategies 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed data - Demo strategies
INSERT INTO strategies (user_id, name, description, symbol, timeframe, config_json, status) 
SELECT 
    u.id,
    'RSI Mean Reversion',
    'Buy when RSI < 30 and price above SMA50, sell when RSI > 70',
    'AAPL',
    '15m',
    '{"indicators":[{"type":"RSI","period":14},{"type":"SMA","period":50}],"entry":{"logic":"AND","rules":[{"indicator":"RSI","operator":"<","value":30},{"indicator":"Price","operator":">","compare_to":"SMA_50"}]},"exit":{"logic":"OR","rules":[{"indicator":"RSI","operator":">","value":70},{"stop_loss":5}]},"position":{"capital_pct":25,"leverage":1}}',
    'ACTIVE'
FROM users u 
WHERE u.role IN ('RESEARCHER', 'PORTFOLIO_MANAGER')
LIMIT 2
ON CONFLICT DO NOTHING;

-- Insert demo strategy tags
INSERT INTO strategy_tags (strategy_id, tag)
SELECT s.id, 'mean-reversion'
FROM strategies s
WHERE s.name = 'RSI Mean Reversion'
ON CONFLICT DO NOTHING;

INSERT INTO strategy_tags (strategy_id, tag)
SELECT s.id, 'intraday'
FROM strategies s
WHERE s.name = 'RSI Mean Reversion'
ON CONFLICT DO NOTHING;

-- Insert corresponding strategy versions
INSERT INTO strategy_versions (strategy_id, version_number, config_json, change_description, author_id)
SELECT 
    s.id,
    1,
    s.config_json,
    'Initial version',
    s.user_id
FROM strategies s
ON CONFLICT DO NOTHING;