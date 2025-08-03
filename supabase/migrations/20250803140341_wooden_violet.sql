/*
# QuantCrux Portfolio Management Schema

1. New Tables
   - `portfolios` - Core portfolio definitions and metadata
   - `portfolio_holdings` - Real-time position tracking
   - `portfolio_history` - Daily NAV and performance history
   - `portfolio_transactions` - Capital flows and trade history

2. Security
   - Enable RLS on all tables
   - Role-based policies for CLIENT and PORTFOLIO_MANAGER access
   - Proper indexes for performance

3. Features
   - Multi-asset portfolio tracking
   - Real-time P&L calculation
   - NAV history and performance analytics
   - Client-PM delegation support
*/

-- Portfolios table - Core portfolio definitions
CREATE TABLE IF NOT EXISTS portfolios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    manager_id UUID REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    initial_capital DECIMAL(15,2) NOT NULL DEFAULT 100000.00,
    current_nav DECIMAL(15,2) NOT NULL DEFAULT 100000.00,
    cash_balance DECIMAL(15,2) NOT NULL DEFAULT 100000.00,
    total_pnl DECIMAL(15,2) DEFAULT 0.00,
    total_return_pct DECIMAL(10,6) DEFAULT 0.00,
    
    -- Risk metrics
    var_95 DECIMAL(15,2), -- Value at Risk (95%, 1-day)
    volatility DECIMAL(8,6), -- Portfolio volatility
    beta DECIMAL(8,6), -- Portfolio beta
    max_drawdown DECIMAL(8,6), -- Maximum drawdown
    sharpe_ratio DECIMAL(8,6), -- Sharpe ratio
    
    -- Status and settings
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    currency VARCHAR(10) DEFAULT 'USD',
    benchmark_symbol VARCHAR(20) DEFAULT 'SPY',
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'ARCHIVED')),
    CONSTRAINT positive_capital CHECK (initial_capital > 0)
);

-- Portfolio holdings table - Real-time position tracking
CREATE TABLE IF NOT EXISTS portfolio_holdings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    instrument_id UUID, -- Can reference strategies, products, or be NULL for direct assets
    instrument_type VARCHAR(20) NOT NULL,
    symbol VARCHAR(50) NOT NULL, -- Asset symbol or identifier
    
    -- Position details
    quantity DECIMAL(20,8) NOT NULL,
    avg_price DECIMAL(15,6) NOT NULL,
    latest_price DECIMAL(15,6),
    market_value DECIMAL(15,2),
    cost_basis DECIMAL(15,2),
    unrealized_pnl DECIMAL(15,2),
    realized_pnl DECIMAL(15,2) DEFAULT 0.00,
    
    -- Metadata
    sector VARCHAR(50),
    asset_class VARCHAR(50),
    weight_pct DECIMAL(8,4), -- % of portfolio
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_instrument_type CHECK (instrument_type IN ('ASSET', 'STRATEGY', 'PRODUCT')),
    CONSTRAINT non_zero_quantity CHECK (quantity != 0)
);

-- Portfolio history table - Daily NAV and performance tracking
CREATE TABLE IF NOT EXISTS portfolio_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    nav DECIMAL(15,2) NOT NULL,
    total_return_pct DECIMAL(10,6),
    daily_return_pct DECIMAL(10,6),
    contributions DECIMAL(15,2) DEFAULT 0.00,
    withdrawals DECIMAL(15,2) DEFAULT 0.00,
    
    -- Performance metrics for the day
    volatility DECIMAL(8,6),
    var_95 DECIMAL(15,2),
    beta DECIMAL(8,6),
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(portfolio_id, date)
);

-- Portfolio transactions table - Capital flows and trade history
CREATE TABLE IF NOT EXISTS portfolio_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    transaction_type VARCHAR(20) NOT NULL,
    symbol VARCHAR(50),
    quantity DECIMAL(20,8),
    price DECIMAL(15,6),
    amount DECIMAL(15,2) NOT NULL,
    fees DECIMAL(15,2) DEFAULT 0.00,
    description TEXT,
    
    -- References to other modules
    strategy_id UUID REFERENCES strategies(id),
    product_id UUID REFERENCES products(id),
    backtest_id UUID REFERENCES backtests(id),
    
    executed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_transaction_type CHECK (transaction_type IN (
        'BUY', 'SELL', 'DEPOSIT', 'WITHDRAWAL', 'DIVIDEND', 'INTEREST', 
        'STRATEGY_ALLOCATION', 'PRODUCT_PURCHASE', 'FEE'
    ))
);

-- Portfolio allocations table - Target vs actual allocations
CREATE TABLE IF NOT EXISTS portfolio_allocations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    allocation_type VARCHAR(20) NOT NULL,
    target_symbol VARCHAR(50),
    target_weight_pct DECIMAL(8,4) NOT NULL,
    actual_weight_pct DECIMAL(8,4) DEFAULT 0.00,
    rebalance_threshold DECIMAL(8,4) DEFAULT 5.00, -- Trigger rebalancing at +/- 5%
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_allocation_type CHECK (allocation_type IN ('ASSET', 'SECTOR', 'STRATEGY', 'PRODUCT')),
    CONSTRAINT valid_weight CHECK (target_weight_pct >= 0 AND target_weight_pct <= 100)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_portfolios_owner_id ON portfolios(owner_id);
CREATE INDEX IF NOT EXISTS idx_portfolios_manager_id ON portfolios(manager_id);
CREATE INDEX IF NOT EXISTS idx_portfolios_status ON portfolios(status);

CREATE INDEX IF NOT EXISTS idx_portfolio_holdings_portfolio_id ON portfolio_holdings(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_holdings_symbol ON portfolio_holdings(symbol);
CREATE INDEX IF NOT EXISTS idx_portfolio_holdings_instrument_type ON portfolio_holdings(instrument_type);
CREATE INDEX IF NOT EXISTS idx_portfolio_holdings_instrument_id ON portfolio_holdings(instrument_id);

CREATE INDEX IF NOT EXISTS idx_portfolio_history_portfolio_id ON portfolio_history(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_history_date ON portfolio_history(date);
CREATE INDEX IF NOT EXISTS idx_portfolio_history_portfolio_date ON portfolio_history(portfolio_id, date);

CREATE INDEX IF NOT EXISTS idx_portfolio_transactions_portfolio_id ON portfolio_transactions(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_transactions_type ON portfolio_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_portfolio_transactions_executed_at ON portfolio_transactions(executed_at);

CREATE INDEX IF NOT EXISTS idx_portfolio_allocations_portfolio_id ON portfolio_allocations(portfolio_id);

-- Enable Row Level Security
ALTER TABLE portfolios ENABLE ROW LEVEL SECURITY;
ALTER TABLE portfolio_holdings ENABLE ROW LEVEL SECURITY;
ALTER TABLE portfolio_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE portfolio_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE portfolio_allocations ENABLE ROW LEVEL SECURITY;

-- RLS Policies for portfolios
CREATE POLICY "Users can read own portfolios" ON portfolios
    FOR SELECT USING (
        owner_id = current_setting('app.current_user_id', true)::UUID OR
        manager_id = current_setting('app.current_user_id', true)::UUID
    );

CREATE POLICY "Users can create portfolios" ON portfolios
    FOR INSERT WITH CHECK (
        owner_id = current_setting('app.current_user_id', true)::UUID OR
        (manager_id = current_setting('app.current_user_id', true)::UUID AND
         EXISTS (
             SELECT 1 FROM users u 
             WHERE u.id = current_setting('app.current_user_id', true)::UUID 
             AND u.role IN ('PORTFOLIO_MANAGER', 'ADMIN')
         ))
    );

CREATE POLICY "Users can update own portfolios" ON portfolios
    FOR UPDATE USING (
        owner_id = current_setting('app.current_user_id', true)::UUID OR
        manager_id = current_setting('app.current_user_id', true)::UUID
    );

CREATE POLICY "Users can delete own portfolios" ON portfolios
    FOR DELETE USING (
        owner_id = current_setting('app.current_user_id', true)::UUID OR
        (manager_id = current_setting('app.current_user_id', true)::UUID AND
         EXISTS (
             SELECT 1 FROM users u 
             WHERE u.id = current_setting('app.current_user_id', true)::UUID 
             AND u.role IN ('PORTFOLIO_MANAGER', 'ADMIN')
         ))
    );

-- RLS Policies for portfolio_holdings
CREATE POLICY "Users can read portfolio holdings" ON portfolio_holdings
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = portfolio_holdings.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )
    );

CREATE POLICY "System can manage portfolio holdings" ON portfolio_holdings
    FOR ALL USING (true);

-- RLS Policies for portfolio_history
CREATE POLICY "Users can read portfolio history" ON portfolio_history
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = portfolio_history.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )
    );

CREATE POLICY "System can manage portfolio history" ON portfolio_history
    FOR ALL USING (true);

-- RLS Policies for portfolio_transactions
CREATE POLICY "Users can read portfolio transactions" ON portfolio_transactions
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = portfolio_transactions.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )
    );

CREATE POLICY "System can manage portfolio transactions" ON portfolio_transactions
    FOR ALL USING (true);

-- RLS Policies for portfolio_allocations
CREATE POLICY "Users can read portfolio allocations" ON portfolio_allocations
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = portfolio_allocations.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )
    );

CREATE POLICY "Users can manage portfolio allocations" ON portfolio_allocations
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = portfolio_allocations.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )
    );

-- Update timestamp triggers
CREATE TRIGGER update_portfolios_updated_at BEFORE UPDATE ON portfolios 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_portfolio_holdings_updated_at BEFORE UPDATE ON portfolio_holdings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_portfolio_allocations_updated_at BEFORE UPDATE ON portfolio_allocations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed sample portfolios for testing
INSERT INTO portfolios (owner_id, name, description, initial_capital, current_nav, cash_balance) 
SELECT 
    u.id,
    'Growth Portfolio 2025',
    'Diversified growth portfolio with tech focus and quantitative strategies',
    100000.00,
    102500.00,
    25000.00
FROM users u 
WHERE u.role = 'CLIENT'
LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO portfolios (owner_id, manager_id, name, description, initial_capital, current_nav, cash_balance) 
SELECT 
    client.id,
    pm.id,
    'Managed Conservative Portfolio',
    'Conservative portfolio managed by professional PM',
    250000.00,
    255750.00,
    50000.00
FROM users client
CROSS JOIN users pm
WHERE client.role = 'CLIENT' AND pm.role = 'PORTFOLIO_MANAGER'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Seed sample holdings
INSERT INTO portfolio_holdings (portfolio_id, instrument_type, symbol, quantity, avg_price, latest_price, market_value, cost_basis, unrealized_pnl, sector, asset_class, weight_pct)
SELECT 
    p.id,
    'ASSET',
    'AAPL',
    100.00,
    150.00,
    158.50,
    15850.00,
    15000.00,
    850.00,
    'Technology',
    'Equity',
    15.85
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO portfolio_holdings (portfolio_id, instrument_type, symbol, quantity, avg_price, latest_price, market_value, cost_basis, unrealized_pnl, sector, asset_class, weight_pct)
SELECT 
    p.id,
    'ASSET',
    'MSFT',
    50.00,
    300.00,
    315.75,
    15787.50,
    15000.00,
    787.50,
    'Technology',
    'Equity',
    15.79
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Seed sample strategy allocation
INSERT INTO portfolio_holdings (portfolio_id, instrument_id, instrument_type, symbol, quantity, avg_price, latest_price, market_value, cost_basis, unrealized_pnl, asset_class, weight_pct)
SELECT 
    p.id,
    s.id,
    'STRATEGY',
    'RSI_MEAN_REVERSION',
    1.00,
    30000.00,
    31200.00,
    31200.00,
    30000.00,
    1200.00,
    'Strategy',
    31.20
FROM portfolios p
CROSS JOIN strategies s
WHERE p.name = 'Growth Portfolio 2025' AND s.name = 'RSI Mean Reversion'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Seed sample portfolio history
INSERT INTO portfolio_history (portfolio_id, date, nav, total_return_pct, daily_return_pct, contributions)
SELECT 
    p.id,
    CURRENT_DATE - INTERVAL '30 days' + (i || ' days')::INTERVAL,
    100000.00 + (i * 50) + (random() * 1000 - 500),
    ((100000.00 + (i * 50)) - 100000.00) / 100000.00,
    CASE WHEN i = 0 THEN 0 ELSE (random() * 0.04 - 0.02) END,
    CASE WHEN i = 0 THEN 100000.00 ELSE 0.00 END
FROM portfolios p
CROSS JOIN generate_series(0, 30) AS i
WHERE p.name = 'Growth Portfolio 2025'
ON CONFLICT (portfolio_id, date) DO NOTHING;

-- Seed sample transactions
INSERT INTO portfolio_transactions (portfolio_id, transaction_type, symbol, quantity, price, amount, description)
SELECT 
    p.id,
    'DEPOSIT',
    NULL,
    NULL,
    NULL,
    100000.00,
    'Initial capital deposit'
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
ON CONFLICT DO NOTHING;

INSERT INTO portfolio_transactions (portfolio_id, transaction_type, symbol, quantity, price, amount, description)
SELECT 
    p.id,
    'BUY',
    'AAPL',
    100.00,
    150.00,
    -15000.00,
    'Purchase of Apple Inc. shares'
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
ON CONFLICT DO NOTHING;