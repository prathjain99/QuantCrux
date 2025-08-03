/*
# QuantCrux Trade Desk Schema

1. New Tables
   - `trades` - Core trade execution records
   - `orders` - Order management and lifecycle tracking
   - `positions` - Aggregated position tracking across portfolios
   - `trade_executions` - Detailed execution logs with slippage tracking

2. Security
   - Enable RLS on all tables
   - Role-based policies for CLIENT and PORTFOLIO_MANAGER access
   - Proper indexes for performance

3. Features
   - Complete trade lifecycle management
   - Real-time position aggregation
   - Order book simulation
   - Execution quality tracking
*/

-- Orders table - Order management and lifecycle
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    instrument_id UUID, -- Can reference strategies, products, or be NULL for direct assets
    instrument_type VARCHAR(20) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    
    -- Order details
    side VARCHAR(10) NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(20,8) NOT NULL,
    limit_price DECIMAL(15,6),
    stop_price DECIMAL(15,6),
    
    -- Execution details
    filled_quantity DECIMAL(20,8) DEFAULT 0,
    avg_fill_price DECIMAL(15,6),
    total_fees DECIMAL(15,2) DEFAULT 0,
    
    -- Status and lifecycle
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    time_in_force VARCHAR(20) DEFAULT 'DAY',
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMPTZ,
    executed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    
    -- Metadata
    notes TEXT,
    client_order_id VARCHAR(100),
    
    CONSTRAINT valid_instrument_type CHECK (instrument_type IN ('ASSET', 'STRATEGY', 'PRODUCT')),
    CONSTRAINT valid_side CHECK (side IN ('BUY', 'SELL')),
    CONSTRAINT valid_order_type CHECK (order_type IN ('MARKET', 'LIMIT', 'STOP', 'STOP_LIMIT', 'CONDITIONAL')),
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'SUBMITTED', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED', 'REJECTED', 'EXPIRED')),
    CONSTRAINT valid_time_in_force CHECK (time_in_force IN ('DAY', 'GTC', 'IOC', 'FOK')),
    CONSTRAINT positive_quantity CHECK (quantity > 0)
);

-- Trades table - Executed trade records
CREATE TABLE IF NOT EXISTS trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    instrument_id UUID,
    instrument_type VARCHAR(20) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    
    -- Trade details
    side VARCHAR(10) NOT NULL,
    quantity DECIMAL(20,8) NOT NULL,
    price DECIMAL(15,6) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    fees DECIMAL(15,2) DEFAULT 0,
    
    -- Execution quality
    expected_price DECIMAL(15,6),
    slippage DECIMAL(15,6),
    execution_venue VARCHAR(50) DEFAULT 'INTERNAL',
    
    -- Status and metadata
    status VARCHAR(20) NOT NULL DEFAULT 'EXECUTED',
    trade_date DATE NOT NULL DEFAULT CURRENT_DATE,
    settlement_date DATE,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    executed_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMPTZ,
    
    -- References
    strategy_id UUID REFERENCES strategies(id),
    product_id UUID REFERENCES products(id),
    
    -- Metadata
    notes TEXT,
    execution_id VARCHAR(100),
    
    CONSTRAINT valid_instrument_type CHECK (instrument_type IN ('ASSET', 'STRATEGY', 'PRODUCT')),
    CONSTRAINT valid_side CHECK (side IN ('BUY', 'SELL')),
    CONSTRAINT valid_status CHECK (status IN ('EXECUTED', 'SETTLED', 'FAILED', 'CANCELLED')),
    CONSTRAINT positive_quantity CHECK (quantity > 0),
    CONSTRAINT positive_price CHECK (price > 0)
);

-- Positions table - Aggregated position tracking
CREATE TABLE IF NOT EXISTS positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    instrument_id UUID,
    instrument_type VARCHAR(20) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    
    -- Position details
    net_quantity DECIMAL(20,8) NOT NULL DEFAULT 0,
    avg_price DECIMAL(15,6),
    cost_basis DECIMAL(15,2),
    market_value DECIMAL(15,2),
    unrealized_pnl DECIMAL(15,2),
    realized_pnl DECIMAL(15,2) DEFAULT 0,
    
    -- Risk metrics
    delta DECIMAL(10,6),
    gamma DECIMAL(10,6),
    theta DECIMAL(10,6),
    vega DECIMAL(10,6),
    
    -- Metadata
    first_trade_date DATE,
    last_trade_date DATE,
    total_trades INTEGER DEFAULT 0,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(portfolio_id, symbol, instrument_type),
    CONSTRAINT valid_instrument_type CHECK (instrument_type IN ('ASSET', 'STRATEGY', 'PRODUCT'))
);

-- Trade executions table - Detailed execution logs
CREATE TABLE IF NOT EXISTS trade_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_id UUID NOT NULL REFERENCES trades(id) ON DELETE CASCADE,
    execution_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    quantity DECIMAL(20,8) NOT NULL,
    price DECIMAL(15,6) NOT NULL,
    venue VARCHAR(50) DEFAULT 'INTERNAL',
    
    -- Execution quality metrics
    bid_price DECIMAL(15,6),
    ask_price DECIMAL(15,6),
    spread DECIMAL(15,6),
    market_impact DECIMAL(15,6),
    
    -- Fees breakdown
    commission DECIMAL(15,6),
    exchange_fee DECIMAL(15,6),
    regulatory_fee DECIMAL(15,6),
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Market quotes table - Real-time price cache
CREATE TABLE IF NOT EXISTS market_quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(50) NOT NULL,
    instrument_type VARCHAR(20) NOT NULL,
    
    -- Price data
    bid_price DECIMAL(15,6),
    ask_price DECIMAL(15,6),
    last_price DECIMAL(15,6) NOT NULL,
    volume DECIMAL(20,2),
    
    -- Market data
    open_price DECIMAL(15,6),
    high_price DECIMAL(15,6),
    low_price DECIMAL(15,6),
    prev_close DECIMAL(15,6),
    
    -- Timestamps
    quote_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    market_date DATE NOT NULL DEFAULT CURRENT_DATE,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(symbol, instrument_type, market_date)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_portfolio_id ON orders(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_orders_symbol ON orders(symbol);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

CREATE INDEX IF NOT EXISTS idx_trades_user_id ON trades(user_id);
CREATE INDEX IF NOT EXISTS idx_trades_portfolio_id ON trades(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_trades_symbol ON trades(symbol);
CREATE INDEX IF NOT EXISTS idx_trades_trade_date ON trades(trade_date);
CREATE INDEX IF NOT EXISTS idx_trades_executed_at ON trades(executed_at);

CREATE INDEX IF NOT EXISTS idx_positions_portfolio_id ON positions(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_positions_symbol ON positions(symbol);
CREATE INDEX IF NOT EXISTS idx_positions_instrument_type ON positions(instrument_type);

CREATE INDEX IF NOT EXISTS idx_trade_executions_trade_id ON trade_executions(trade_id);
CREATE INDEX IF NOT EXISTS idx_trade_executions_execution_time ON trade_executions(execution_time);

CREATE INDEX IF NOT EXISTS idx_market_quotes_symbol ON market_quotes(symbol);
CREATE INDEX IF NOT EXISTS idx_market_quotes_quote_time ON market_quotes(quote_time);

-- Enable Row Level Security
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE trades ENABLE ROW LEVEL SECURITY;
ALTER TABLE positions ENABLE ROW LEVEL SECURITY;
ALTER TABLE trade_executions ENABLE ROW LEVEL SECURITY;
ALTER TABLE market_quotes ENABLE ROW LEVEL SECURITY;

-- RLS Policies for orders
CREATE POLICY "Users can read own orders" ON orders
    FOR SELECT USING (
        user_id = current_setting('app.current_user_id', true)::UUID OR
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = orders.portfolio_id 
            AND p.manager_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "Users can create orders" ON orders
    FOR INSERT WITH CHECK (
        user_id = current_setting('app.current_user_id', true)::UUID OR
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = orders.portfolio_id 
            AND p.manager_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "Users can update own orders" ON orders
    FOR UPDATE USING (
        user_id = current_setting('app.current_user_id', true)::UUID OR
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = orders.portfolio_id 
            AND p.manager_id = current_setting('app.current_user_id', true)::UUID
        )
    );

-- RLS Policies for trades
CREATE POLICY "Users can read own trades" ON trades
    FOR SELECT USING (
        user_id = current_setting('app.current_user_id', true)::UUID OR
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = trades.portfolio_id 
            AND p.manager_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "System can manage trades" ON trades
    FOR ALL USING (true);

-- RLS Policies for positions
CREATE POLICY "Users can read portfolio positions" ON positions
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = positions.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )
    );

CREATE POLICY "System can manage positions" ON positions
    FOR ALL USING (true);

-- RLS Policies for trade_executions
CREATE POLICY "Users can read own trade executions" ON trade_executions
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM trades t 
            WHERE t.id = trade_executions.trade_id 
            AND (t.user_id = current_setting('app.current_user_id', true)::UUID OR
                 EXISTS (
                     SELECT 1 FROM portfolios p 
                     WHERE p.id = t.portfolio_id 
                     AND p.manager_id = current_setting('app.current_user_id', true)::UUID
                 ))
        )
    );

-- RLS Policies for market_quotes (readable by all authenticated users)
CREATE POLICY "Authenticated users can read market quotes" ON market_quotes
    FOR SELECT TO authenticated USING (true);

CREATE POLICY "System can manage market quotes" ON market_quotes
    FOR ALL USING (true);

-- Update timestamp triggers
CREATE TRIGGER update_positions_updated_at BEFORE UPDATE ON positions 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_market_quotes_updated_at BEFORE UPDATE ON market_quotes 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed sample market quotes
INSERT INTO market_quotes (symbol, instrument_type, bid_price, ask_price, last_price, volume, open_price, high_price, low_price, prev_close) VALUES
('AAPL', 'ASSET', 169.50, 169.52, 169.51, 1500000, 168.75, 170.25, 168.50, 168.90),
('MSFT', 'ASSET', 314.25, 314.27, 314.26, 800000, 313.50, 315.00, 313.25, 313.75),
('GOOGL', 'ASSET', 2485.50, 2485.75, 2485.62, 250000, 2480.00, 2490.00, 2478.50, 2482.25),
('TSLA', 'ASSET', 198.75, 198.80, 198.77, 2000000, 197.50, 199.50, 197.25, 198.00),
('BTCUSD', 'ASSET', 44950.00, 45050.00, 45000.00, 125.50, 44800.00, 45200.00, 44750.00, 44875.00),
('ETHUSD', 'ASSET', 2995.50, 3004.50, 3000.00, 850.25, 2980.00, 3010.00, 2975.00, 2985.00)
ON CONFLICT (symbol, instrument_type, market_date) DO UPDATE SET
    bid_price = EXCLUDED.bid_price,
    ask_price = EXCLUDED.ask_price,
    last_price = EXCLUDED.last_price,
    volume = EXCLUDED.volume,
    updated_at = CURRENT_TIMESTAMP;

-- Seed sample orders and trades for testing
INSERT INTO orders (user_id, portfolio_id, instrument_type, symbol, side, order_type, quantity, status, filled_quantity, avg_fill_price, executed_at)
SELECT 
    p.owner_id,
    p.id,
    'ASSET',
    'AAPL',
    'BUY',
    'MARKET',
    100.00,
    'FILLED',
    100.00,
    169.51,
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO trades (order_id, user_id, portfolio_id, instrument_type, symbol, side, quantity, price, total_amount, fees, expected_price, slippage, status, executed_at)
SELECT 
    o.id,
    o.user_id,
    o.portfolio_id,
    o.instrument_type,
    o.symbol,
    o.side,
    o.filled_quantity,
    o.avg_fill_price,
    o.filled_quantity * o.avg_fill_price,
    o.filled_quantity * o.avg_fill_price * 0.001, -- 0.1% commission
    169.50,
    0.01, -- 1 cent slippage
    'EXECUTED',
    o.executed_at
FROM orders o
WHERE o.symbol = 'AAPL' AND o.status = 'FILLED'
ON CONFLICT DO NOTHING;

-- Update positions based on trades
INSERT INTO positions (portfolio_id, instrument_type, symbol, net_quantity, avg_price, cost_basis, first_trade_date, last_trade_date, total_trades)
SELECT 
    t.portfolio_id,
    t.instrument_type,
    t.symbol,
    SUM(CASE WHEN t.side = 'BUY' THEN t.quantity ELSE -t.quantity END),
    AVG(t.price),
    SUM(CASE WHEN t.side = 'BUY' THEN t.total_amount ELSE -t.total_amount END),
    MIN(t.trade_date),
    MAX(t.trade_date),
    COUNT(*)
FROM trades t
WHERE t.status = 'EXECUTED'
GROUP BY t.portfolio_id, t.instrument_type, t.symbol
ON CONFLICT (portfolio_id, symbol, instrument_type) DO UPDATE SET
    net_quantity = EXCLUDED.net_quantity,
    avg_price = EXCLUDED.avg_price,
    cost_basis = EXCLUDED.cost_basis,
    last_trade_date = EXCLUDED.last_trade_date,
    total_trades = EXCLUDED.total_trades,
    updated_at = CURRENT_TIMESTAMP;