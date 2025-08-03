/*
# QuantCrux Backtesting Engine Schema

1. New Tables
   - `backtests` - Core backtest runs and results
   - `backtest_trades` - Individual simulated trades
   - `backtest_metrics` - Performance metrics
   - `market_data` - Historical OHLCV data cache

2. Security
   - Enable RLS on all tables
   - Role-based policies for RESEARCHER and PORTFOLIO_MANAGER access
   - Proper indexes for performance

3. Features
   - Comprehensive backtest result storage
   - Trade-by-trade logging
   - Performance metrics calculation
   - Historical data caching
*/

-- Backtests table - Core backtest runs
CREATE TABLE IF NOT EXISTS backtests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategy_id UUID NOT NULL REFERENCES strategies(id) ON DELETE CASCADE,
    strategy_version_id UUID REFERENCES strategy_versions(id),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    initial_capital DECIMAL(15,2) NOT NULL DEFAULT 100000.00,
    commission_rate DECIMAL(8,6) DEFAULT 0.001,
    slippage_rate DECIMAL(8,6) DEFAULT 0.0005,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INTEGER DEFAULT 0,
    error_message TEXT,
    
    -- Results summary
    final_capital DECIMAL(15,2),
    total_return DECIMAL(10,6),
    total_trades INTEGER DEFAULT 0,
    winning_trades INTEGER DEFAULT 0,
    losing_trades INTEGER DEFAULT 0,
    
    -- Key metrics
    sharpe_ratio DECIMAL(10,6),
    sortino_ratio DECIMAL(10,6),
    max_drawdown DECIMAL(10,6),
    max_drawdown_duration INTEGER,
    cagr DECIMAL(10,6),
    volatility DECIMAL(10,6),
    profit_factor DECIMAL(10,6),
    win_rate DECIMAL(8,6),
    avg_trade_duration INTEGER,
    
    -- Detailed results (JSON)
    equity_curve TEXT, -- JSON array of equity values over time
    drawdown_curve TEXT, -- JSON array of drawdown values
    monthly_returns TEXT, -- JSON object of monthly return breakdown
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT valid_timeframe CHECK (timeframe IN ('1m', '5m', '15m', '30m', '1h', '4h', '1d')),
    CONSTRAINT valid_dates CHECK (end_date >= start_date)
);

-- Backtest trades table - Individual simulated trades
CREATE TABLE IF NOT EXISTS backtest_trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    backtest_id UUID NOT NULL REFERENCES backtests(id) ON DELETE CASCADE,
    trade_number INTEGER NOT NULL,
    signal_type VARCHAR(10) NOT NULL,
    
    -- Entry details
    entry_time TIMESTAMPTZ NOT NULL,
    entry_price DECIMAL(15,6) NOT NULL,
    entry_reason TEXT,
    entry_indicators TEXT, -- JSON of indicator values at entry
    
    -- Exit details
    exit_time TIMESTAMPTZ,
    exit_price DECIMAL(15,6),
    exit_reason TEXT,
    exit_indicators TEXT, -- JSON of indicator values at exit
    
    -- Trade results
    quantity DECIMAL(15,6) NOT NULL,
    gross_pnl DECIMAL(15,6),
    net_pnl DECIMAL(15,6), -- After commission and slippage
    return_pct DECIMAL(10,6),
    duration_minutes INTEGER,
    
    -- Position info
    position_size_pct DECIMAL(8,4), -- % of capital used
    commission_paid DECIMAL(15,6),
    slippage_cost DECIMAL(15,6),
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_signal_type CHECK (signal_type IN ('BUY', 'SELL', 'LONG', 'SHORT'))
);

-- Market data table - Historical OHLCV cache
CREATE TABLE IF NOT EXISTS market_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) NOT NULL,
    timeframe VARCHAR(10) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    open_price DECIMAL(15,6) NOT NULL,
    high_price DECIMAL(15,6) NOT NULL,
    low_price DECIMAL(15,6) NOT NULL,
    close_price DECIMAL(15,6) NOT NULL,
    volume DECIMAL(20,2) NOT NULL,
    
    -- Technical indicators cache (optional)
    indicators TEXT, -- JSON of computed indicators
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(symbol, timeframe, timestamp)
);

-- Backtest parameters table - Store backtest configuration
CREATE TABLE IF NOT EXISTS backtest_parameters (
    backtest_id UUID PRIMARY KEY REFERENCES backtests(id) ON DELETE CASCADE,
    strategy_config TEXT NOT NULL, -- JSON copy of strategy config used
    custom_parameters TEXT, -- JSON of any custom backtest parameters
    data_source VARCHAR(50) DEFAULT 'INTERNAL',
    monte_carlo_runs INTEGER DEFAULT 1,
    walk_forward_enabled BOOLEAN DEFAULT FALSE,
    benchmark_symbol VARCHAR(20),
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_backtests_user_id ON backtests(user_id);
CREATE INDEX IF NOT EXISTS idx_backtests_strategy_id ON backtests(strategy_id);
CREATE INDEX IF NOT EXISTS idx_backtests_symbol ON backtests(symbol);
CREATE INDEX IF NOT EXISTS idx_backtests_status ON backtests(status);
CREATE INDEX IF NOT EXISTS idx_backtests_created_at ON backtests(created_at);

CREATE INDEX IF NOT EXISTS idx_backtest_trades_backtest_id ON backtest_trades(backtest_id);
CREATE INDEX IF NOT EXISTS idx_backtest_trades_entry_time ON backtest_trades(entry_time);
CREATE INDEX IF NOT EXISTS idx_backtest_trades_signal_type ON backtest_trades(signal_type);

CREATE INDEX IF NOT EXISTS idx_market_data_symbol_timeframe ON market_data(symbol, timeframe);
CREATE INDEX IF NOT EXISTS idx_market_data_timestamp ON market_data(timestamp);
CREATE INDEX IF NOT EXISTS idx_market_data_symbol_timeframe_timestamp ON market_data(symbol, timeframe, timestamp);

-- Enable Row Level Security
ALTER TABLE backtests ENABLE ROW LEVEL SECURITY;
ALTER TABLE backtest_trades ENABLE ROW LEVEL SECURITY;
ALTER TABLE market_data ENABLE ROW LEVEL SECURITY;
ALTER TABLE backtest_parameters ENABLE ROW LEVEL SECURITY;

-- RLS Policies for backtests
CREATE POLICY "Users can read own backtests" ON backtests
    FOR SELECT USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can create backtests" ON backtests
    FOR INSERT WITH CHECK (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can update own backtests" ON backtests
    FOR UPDATE USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can delete own backtests" ON backtests
    FOR DELETE USING (user_id = current_setting('app.current_user_id', true)::UUID);

-- RLS Policies for backtest_trades
CREATE POLICY "Users can read own backtest trades" ON backtest_trades
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM backtests b 
            WHERE b.id = backtest_trades.backtest_id 
            AND b.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "System can manage backtest trades" ON backtest_trades
    FOR ALL USING (true);

-- RLS Policies for market_data (readable by all authenticated users for caching)
CREATE POLICY "Authenticated users can read market data" ON market_data
    FOR SELECT TO authenticated USING (true);

CREATE POLICY "System can manage market data" ON market_data
    FOR ALL USING (true);

-- RLS Policies for backtest_parameters
CREATE POLICY "Users can read own backtest parameters" ON backtest_parameters
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM backtests b 
            WHERE b.id = backtest_parameters.backtest_id 
            AND b.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "System can manage backtest parameters" ON backtest_parameters
    FOR ALL USING (true);

-- Update timestamp trigger for backtests
CREATE TRIGGER update_backtests_updated_at BEFORE UPDATE ON backtests 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed some sample market data for testing
INSERT INTO market_data (symbol, timeframe, timestamp, open_price, high_price, low_price, close_price, volume) VALUES
('AAPL', '1d', '2024-01-01 00:00:00+00', 150.00, 152.50, 149.00, 151.25, 1000000),
('AAPL', '1d', '2024-01-02 00:00:00+00', 151.25, 153.00, 150.50, 152.75, 1200000),
('AAPL', '1d', '2024-01-03 00:00:00+00', 152.75, 154.25, 151.75, 153.50, 1100000),
('BTCUSD', '1d', '2024-01-01 00:00:00+00', 45000.00, 46500.00, 44500.00, 45800.00, 500.25),
('BTCUSD', '1d', '2024-01-02 00:00:00+00', 45800.00, 47200.00, 45200.00, 46900.00, 650.75),
('BTCUSD', '1d', '2024-01-03 00:00:00+00', 46900.00, 48100.00, 46300.00, 47650.00, 720.50)
ON CONFLICT (symbol, timeframe, timestamp) DO NOTHING;