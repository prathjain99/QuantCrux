/*
# QuantCrux Strategy Builder Schema

1. New Tables
   - `strategies` - Core strategy definitions with JSONB config
   - `strategy_versions` - Version control for strategy changes
   - `strategy_indicators` - Cache for computed indicators
   - `strategy_signals` - Historical signal logs

2. Security
   - Enable RLS on all tables
   - Role-based policies for RESEARCHER and PORTFOLIO_MANAGER access
   - Proper indexes for performance

3. Features
   - JSONB storage for flexible strategy configuration
   - Version control with rollback capability
   - Real-time indicator caching
   - Signal history tracking
*/

-- Create strategy status enum
CREATE TYPE strategy_status AS ENUM ('DRAFT', 'ACTIVE', 'PAUSED', 'ARCHIVED');
CREATE TYPE signal_type AS ENUM ('BUY', 'SELL', 'HOLD', 'NO_SIGNAL');

-- Strategies table
CREATE TABLE strategies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    symbol VARCHAR(20) NOT NULL,
    timeframe VARCHAR(10) NOT NULL DEFAULT '1m',
    config_json JSONB NOT NULL,
    status strategy_status NOT NULL DEFAULT 'DRAFT',
    tags TEXT[],
    current_version INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_timeframe CHECK (timeframe IN ('1m', '5m', '15m', '30m', '1h', '4h', '1d'))
);

-- Strategy versions for version control
CREATE TABLE strategy_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_id UUID NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    config_json JSONB NOT NULL,
    change_description TEXT,
    author_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(strategy_id, version_number)
);

-- Strategy indicators cache for real-time computation
CREATE TABLE strategy_indicators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    indicator_data JSONB NOT NULL,
    price_data JSONB NOT NULL,
    computed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(symbol, timeframe)
);

-- Strategy signals log
CREATE TABLE strategy_signals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_id UUID NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    signal_type signal_type NOT NULL,
    price DECIMAL(15,6),
    indicator_values JSONB,
    matched_rules TEXT[],
    confidence_score DECIMAL(5,4),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_strategies_user_id ON strategies(user_id);
CREATE INDEX idx_strategies_symbol ON strategies(symbol);
CREATE INDEX idx_strategies_status ON strategies(status);
CREATE INDEX idx_strategies_tags ON strategies USING GIN(tags);
CREATE INDEX idx_strategy_versions_strategy_id ON strategy_versions(strategy_id);
CREATE INDEX idx_strategy_indicators_symbol_timeframe ON strategy_indicators(symbol, timeframe);
CREATE INDEX idx_strategy_signals_strategy_id ON strategy_signals(strategy_id);
CREATE INDEX idx_strategy_signals_created_at ON strategy_signals(created_at);

-- Enable Row Level Security
ALTER TABLE strategies ENABLE ROW LEVEL SECURITY;
ALTER TABLE strategy_versions ENABLE ROW LEVEL SECURITY;
ALTER TABLE strategy_indicators ENABLE ROW LEVEL SECURITY;
ALTER TABLE strategy_signals ENABLE ROW LEVEL SECURITY;

-- RLS Policies for strategies
CREATE POLICY "Users can read own strategies" ON strategies
    FOR SELECT USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can create strategies" ON strategies
    FOR INSERT WITH CHECK (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can update own strategies" ON strategies
    FOR UPDATE USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can delete own strategies" ON strategies
    FOR DELETE USING (user_id = current_setting('app.current_user_id', true)::UUID);

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

-- RLS Policies for strategy_indicators (readable by all authenticated users)
CREATE POLICY "Authenticated users can read indicators" ON strategy_indicators
    FOR SELECT TO authenticated USING (true);

CREATE POLICY "System can manage indicators" ON strategy_indicators
    FOR ALL USING (true);

-- RLS Policies for strategy_signals
CREATE POLICY "Users can read own strategy signals" ON strategy_signals
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM strategies s 
            WHERE s.id = strategy_signals.strategy_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

-- Update timestamp trigger for strategies
CREATE TRIGGER update_strategies_updated_at BEFORE UPDATE ON strategies 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed data - Demo strategies
INSERT INTO strategies (user_id, name, description, symbol, timeframe, config_json, status, tags) 
SELECT 
    u.id,
    'RSI Mean Reversion',
    'Buy when RSI < 30 and price above SMA50, sell when RSI > 70',
    'AAPL',
    '15m',
    '{
        "indicators": [
            {"type": "RSI", "period": 14},
            {"type": "SMA", "period": 50}
        ],
        "entry": {
            "logic": "AND",
            "rules": [
                {"indicator": "RSI", "operator": "<", "value": 30},
                {"indicator": "Price", "operator": ">", "compare_to": "SMA_50"}
            ]
        },
        "exit": {
            "logic": "OR", 
            "rules": [
                {"indicator": "RSI", "operator": ">", "value": 70},
                {"stop_loss": 5}
            ]
        },
        "position": {
            "capital_pct": 25,
            "leverage": 1
        }
    }'::jsonb,
    'ACTIVE',
    ARRAY['mean-reversion', 'intraday']
FROM users u 
WHERE u.role IN ('RESEARCHER', 'PORTFOLIO_MANAGER')
LIMIT 2;

-- Insert corresponding strategy versions
INSERT INTO strategy_versions (strategy_id, version_number, config_json, change_description, author_id)
SELECT 
    s.id,
    1,
    s.config_json,
    'Initial version',
    s.user_id
FROM strategies s;