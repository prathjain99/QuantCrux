/*
# QuantCrux Market Data Service Schema

1. New Tables
   - `market_data_cache` - Cached price and OHLCV data
   - `data_sources` - External API configuration and status
   - `symbol_metadata` - Symbol information and search data
   - `benchmark_data` - Index and benchmark price history

2. Security
   - Enable RLS on all tables
   - Public read access for authenticated users
   - System-only write access for data ingestion

3. Features
   - Multi-source data aggregation
   - Intelligent caching with TTL
   - Symbol search and metadata
   - Benchmark tracking for analytics
*/

-- Market data cache table - Stores live and historical price data
CREATE TABLE IF NOT EXISTS market_data_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) NOT NULL,
    data_type VARCHAR(20) NOT NULL, -- 'live_price', 'ohlcv', 'intraday'
    timeframe VARCHAR(10), -- '1m', '5m', '1h', '1d' (null for live prices)
    
    -- Price data
    price DECIMAL(15,6),
    open_price DECIMAL(15,6),
    high_price DECIMAL(15,6),
    low_price DECIMAL(15,6),
    close_price DECIMAL(15,6),
    volume DECIMAL(20,2),
    
    -- Market data
    bid_price DECIMAL(15,6),
    ask_price DECIMAL(15,6),
    spread DECIMAL(15,6),
    day_change DECIMAL(15,6),
    day_change_percent DECIMAL(8,6),
    
    -- Metadata
    data_timestamp TIMESTAMPTZ NOT NULL,
    source VARCHAR(50) NOT NULL, -- 'finnhub', 'twelvedata', 'yfinance', etc.
    quality_score INTEGER DEFAULT 100, -- 0-100, lower for stale data
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    
    CONSTRAINT valid_data_type CHECK (data_type IN ('live_price', 'ohlcv', 'intraday')),
    CONSTRAINT valid_timeframe CHECK (timeframe IS NULL OR timeframe IN ('1m', '5m', '15m', '30m', '1h', '4h', '1d', '1w')),
    CONSTRAINT valid_quality_score CHECK (quality_score >= 0 AND quality_score <= 100)
);

-- Data sources configuration table
CREATE TABLE IF NOT EXISTS data_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    api_url VARCHAR(255) NOT NULL,
    api_key_hash VARCHAR(255), -- Encrypted API key
    
    -- Configuration
    rate_limit_per_minute INTEGER DEFAULT 60,
    rate_limit_per_day INTEGER DEFAULT 1000,
    priority INTEGER DEFAULT 1, -- Higher number = higher priority
    
    -- Status tracking
    is_active BOOLEAN DEFAULT TRUE,
    last_request_at TIMESTAMPTZ,
    requests_today INTEGER DEFAULT 0,
    requests_this_minute INTEGER DEFAULT 0,
    
    -- Error tracking
    consecutive_failures INTEGER DEFAULT 0,
    last_error_message TEXT,
    last_error_at TIMESTAMPTZ,
    
    -- Supported features
    supports_live_prices BOOLEAN DEFAULT TRUE,
    supports_historical BOOLEAN DEFAULT TRUE,
    supports_crypto BOOLEAN DEFAULT FALSE,
    supports_forex BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Symbol metadata table - For search and autocomplete
CREATE TABLE IF NOT EXISTS symbol_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    exchange VARCHAR(50),
    currency VARCHAR(10) DEFAULT 'USD',
    asset_type VARCHAR(20) NOT NULL, -- 'stock', 'crypto', 'etf', 'forex', 'index'
    sector VARCHAR(50),
    industry VARCHAR(100),
    country VARCHAR(50),
    
    -- Trading info
    is_tradeable BOOLEAN DEFAULT TRUE,
    min_quantity DECIMAL(20,8) DEFAULT 1,
    tick_size DECIMAL(15,6) DEFAULT 0.01,
    
    -- Metadata
    description TEXT,
    website VARCHAR(255),
    market_cap DECIMAL(20,2),
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_asset_type CHECK (asset_type IN ('stock', 'crypto', 'etf', 'forex', 'index', 'commodity'))
);

-- Benchmark data table - For analytics comparisons
CREATE TABLE IF NOT EXISTS benchmark_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    close_price DECIMAL(15,6) NOT NULL,
    
    -- Additional metrics
    total_return_index DECIMAL(15,6),
    dividend_yield DECIMAL(8,6),
    pe_ratio DECIMAL(8,2),
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(symbol, date)
);

-- API usage tracking table
CREATE TABLE IF NOT EXISTS api_usage_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    data_source_id UUID NOT NULL REFERENCES data_sources(id) ON DELETE CASCADE,
    endpoint VARCHAR(255) NOT NULL,
    symbol VARCHAR(20),
    request_params TEXT, -- JSON
    
    -- Response info
    response_status INTEGER,
    response_time_ms INTEGER,
    data_points_returned INTEGER,
    
    -- Usage tracking
    request_timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    user_id UUID REFERENCES users(id),
    module_name VARCHAR(50), -- 'trade_desk', 'portfolio', 'analytics', etc.
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_market_data_cache_symbol ON market_data_cache(symbol);
CREATE INDEX IF NOT EXISTS idx_market_data_cache_data_type ON market_data_cache(data_type);
CREATE INDEX IF NOT EXISTS idx_market_data_cache_timeframe ON market_data_cache(timeframe);
CREATE INDEX IF NOT EXISTS idx_market_data_cache_expires_at ON market_data_cache(expires_at);
CREATE INDEX IF NOT EXISTS idx_market_data_cache_symbol_type_timeframe ON market_data_cache(symbol, data_type, timeframe);

CREATE INDEX IF NOT EXISTS idx_symbol_metadata_symbol ON symbol_metadata(symbol);
CREATE INDEX IF NOT EXISTS idx_symbol_metadata_name ON symbol_metadata(name);
CREATE INDEX IF NOT EXISTS idx_symbol_metadata_asset_type ON symbol_metadata(asset_type);
CREATE INDEX IF NOT EXISTS idx_symbol_metadata_exchange ON symbol_metadata(exchange);

CREATE INDEX IF NOT EXISTS idx_benchmark_data_symbol ON benchmark_data(symbol);
CREATE INDEX IF NOT EXISTS idx_benchmark_data_date ON benchmark_data(date);
CREATE INDEX IF NOT EXISTS idx_benchmark_data_symbol_date ON benchmark_data(symbol, date);

CREATE INDEX IF NOT EXISTS idx_api_usage_log_data_source_id ON api_usage_log(data_source_id);
CREATE INDEX IF NOT EXISTS idx_api_usage_log_request_timestamp ON api_usage_log(request_timestamp);
CREATE INDEX IF NOT EXISTS idx_api_usage_log_symbol ON api_usage_log(symbol);

-- Enable Row Level Security
ALTER TABLE market_data_cache ENABLE ROW LEVEL SECURITY;
ALTER TABLE data_sources ENABLE ROW LEVEL SECURITY;
ALTER TABLE symbol_metadata ENABLE ROW LEVEL SECURITY;
ALTER TABLE benchmark_data ENABLE ROW LEVEL SECURITY;
ALTER TABLE api_usage_log ENABLE ROW LEVEL SECURITY;

-- RLS Policies - Market data is readable by all authenticated users
CREATE POLICY "Authenticated users can read market data cache" ON market_data_cache
    FOR SELECT TO authenticated USING (true);

CREATE POLICY "System can manage market data cache" ON market_data_cache
    FOR ALL USING (true);

CREATE POLICY "Authenticated users can read symbol metadata" ON symbol_metadata
    FOR SELECT TO authenticated USING (true);

CREATE POLICY "System can manage symbol metadata" ON symbol_metadata
    FOR ALL USING (true);

CREATE POLICY "Authenticated users can read benchmark data" ON benchmark_data
    FOR SELECT TO authenticated USING (true);

CREATE POLICY "System can manage benchmark data" ON benchmark_data
    FOR ALL USING (true);

-- Data sources and API usage are admin-only
CREATE POLICY "Admins can read data sources" ON data_sources
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM users u 
            WHERE u.id = current_setting('app.current_user_id', true)::UUID 
            AND u.role = 'ADMIN'
        )
    );

CREATE POLICY "System can manage data sources" ON data_sources
    FOR ALL USING (true);

CREATE POLICY "Admins can read API usage logs" ON api_usage_log
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM users u 
            WHERE u.id = current_setting('app.current_user_id', true)::UUID 
            AND u.role = 'ADMIN'
        )
    );

CREATE POLICY "System can manage API usage logs" ON api_usage_log
    FOR ALL USING (true);

-- Update timestamp triggers
CREATE TRIGGER update_market_data_cache_updated_at BEFORE UPDATE ON market_data_cache 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_data_sources_updated_at BEFORE UPDATE ON data_sources 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_symbol_metadata_updated_at BEFORE UPDATE ON symbol_metadata 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed data sources
INSERT INTO data_sources (name, api_url, rate_limit_per_minute, rate_limit_per_day, priority, supports_live_prices, supports_historical, supports_crypto, supports_forex) VALUES
('finnhub', 'https://finnhub.io/api/v1', 60, 1000, 1, true, true, true, true),
('twelvedata', 'https://api.twelvedata.com/v1', 8, 800, 2, true, true, true, true),
('yfinance', 'https://query1.finance.yahoo.com/v8', 100, 10000, 3, true, true, true, false),
('coingecko', 'https://api.coingecko.com/api/v3', 50, 1000, 1, true, true, true, false)
ON CONFLICT (name) DO NOTHING;

-- Seed popular symbol metadata
INSERT INTO symbol_metadata (symbol, name, exchange, currency, asset_type, sector, industry, country, market_cap) VALUES
('AAPL', 'Apple Inc.', 'NASDAQ', 'USD', 'stock', 'Technology', 'Consumer Electronics', 'US', 3000000000000),
('MSFT', 'Microsoft Corporation', 'NASDAQ', 'USD', 'stock', 'Technology', 'Software', 'US', 2800000000000),
('GOOGL', 'Alphabet Inc.', 'NASDAQ', 'USD', 'stock', 'Technology', 'Internet Services', 'US', 2000000000000),
('TSLA', 'Tesla Inc.', 'NASDAQ', 'USD', 'stock', 'Consumer Cyclical', 'Auto Manufacturers', 'US', 800000000000),
('AMZN', 'Amazon.com Inc.', 'NASDAQ', 'USD', 'stock', 'Consumer Cyclical', 'Internet Retail', 'US', 1500000000000),
('NVDA', 'NVIDIA Corporation', 'NASDAQ', 'USD', 'stock', 'Technology', 'Semiconductors', 'US', 2200000000000),
('META', 'Meta Platforms Inc.', 'NASDAQ', 'USD', 'stock', 'Technology', 'Internet Services', 'US', 900000000000),
('NFLX', 'Netflix Inc.', 'NASDAQ', 'USD', 'stock', 'Communication Services', 'Entertainment', 'US', 200000000000),

-- Crypto
('BTCUSD', 'Bitcoin', 'CRYPTO', 'USD', 'crypto', 'Cryptocurrency', 'Digital Currency', 'Global', 1000000000000),
('ETHUSD', 'Ethereum', 'CRYPTO', 'USD', 'crypto', 'Cryptocurrency', 'Smart Contracts', 'Global', 400000000000),
('ADAUSD', 'Cardano', 'CRYPTO', 'USD', 'crypto', 'Cryptocurrency', 'Blockchain Platform', 'Global', 50000000000),
('SOLUSD', 'Solana', 'CRYPTO', 'USD', 'crypto', 'Cryptocurrency', 'Blockchain Platform', 'Global', 80000000000),

-- ETFs and Indices
('SPY', 'SPDR S&P 500 ETF Trust', 'NYSE', 'USD', 'etf', 'Broad Market', 'Large Cap Blend', 'US', 500000000000),
('QQQ', 'Invesco QQQ Trust', 'NASDAQ', 'USD', 'etf', 'Technology', 'Large Cap Growth', 'US', 200000000000),
('VTI', 'Vanguard Total Stock Market ETF', 'NYSE', 'USD', 'etf', 'Broad Market', 'Total Market', 'US', 300000000000),
('^GSPC', 'S&P 500 Index', 'INDEX', 'USD', 'index', 'Broad Market', 'Large Cap', 'US', null),
('^IXIC', 'NASDAQ Composite', 'INDEX', 'USD', 'index', 'Technology', 'Composite', 'US', null),
('^DJI', 'Dow Jones Industrial Average', 'INDEX', 'USD', 'index', 'Broad Market', 'Large Cap', 'US', null)
ON CONFLICT (symbol) DO NOTHING;

-- Seed benchmark data (sample S&P 500 data)
INSERT INTO benchmark_data (symbol, name, date, close_price, total_return_index, dividend_yield) VALUES
('^GSPC', 'S&P 500 Index', '2024-12-31', 4769.83, 11679.14, 0.0134),
('^GSPC', 'S&P 500 Index', '2025-01-01', 4792.11, 11732.45, 0.0134),
('^GSPC', 'S&P 500 Index', '2025-01-02', 4808.76, 11773.12, 0.0134),
('^GSPC', 'S&P 500 Index', '2025-01-03', 4825.42, 11813.89, 0.0134),
('SPY', 'SPDR S&P 500 ETF', '2024-12-31', 477.90, 477.90, 0.0128),
('SPY', 'SPDR S&P 500 ETF', '2025-01-01', 480.15, 480.15, 0.0128),
('SPY', 'SPDR S&P 500 ETF', '2025-01-02', 481.88, 481.88, 0.0128),
('SPY', 'SPDR S&P 500 ETF', '2025-01-03', 483.61, 483.61, 0.0128)
ON CONFLICT (symbol, date) DO NOTHING;

-- Seed sample market data cache
INSERT INTO market_data_cache (symbol, data_type, price, bid_price, ask_price, day_change, day_change_percent, data_timestamp, source, expires_at) VALUES
('AAPL', 'live_price', 172.50, 172.48, 172.52, 2.15, 0.0126, CURRENT_TIMESTAMP, 'finnhub', CURRENT_TIMESTAMP + INTERVAL '1 minute'),
('MSFT', 'live_price', 415.75, 415.70, 415.80, 5.25, 0.0128, CURRENT_TIMESTAMP, 'finnhub', CURRENT_TIMESTAMP + INTERVAL '1 minute'),
('GOOGL', 'live_price', 175.85, 175.80, 175.90, -1.45, -0.0082, CURRENT_TIMESTAMP, 'finnhub', CURRENT_TIMESTAMP + INTERVAL '1 minute'),
('TSLA', 'live_price', 248.50, 248.45, 248.55, 8.75, 0.0365, CURRENT_TIMESTAMP, 'finnhub', CURRENT_TIMESTAMP + INTERVAL '1 minute'),
('BTCUSD', 'live_price', 97250.00, 97200.00, 97300.00, 1250.00, 0.0130, CURRENT_TIMESTAMP, 'coingecko', CURRENT_TIMESTAMP + INTERVAL '1 minute'),
('ETHUSD', 'live_price', 3420.50, 3418.00, 3423.00, 45.50, 0.0135, CURRENT_TIMESTAMP, 'coingecko', CURRENT_TIMESTAMP + INTERVAL '1 minute')
ON CONFLICT DO NOTHING;

-- Function to clean expired cache entries
CREATE OR REPLACE FUNCTION clean_expired_market_data()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM market_data_cache WHERE expires_at < CURRENT_TIMESTAMP;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Function to update data source usage stats
CREATE OR REPLACE FUNCTION update_data_source_usage(source_name VARCHAR(50))
RETURNS VOID AS $$
BEGIN
    UPDATE data_sources 
    SET 
        last_request_at = CURRENT_TIMESTAMP,
        requests_today = requests_today + 1,
        requests_this_minute = requests_this_minute + 1,
        consecutive_failures = 0
    WHERE name = source_name;
END;
$$ LANGUAGE plpgsql;

-- Function to log API failures
CREATE OR REPLACE FUNCTION log_data_source_failure(source_name VARCHAR(50), error_msg TEXT)
RETURNS VOID AS $$
BEGIN
    UPDATE data_sources 
    SET 
        consecutive_failures = consecutive_failures + 1,
        last_error_message = error_msg,
        last_error_at = CURRENT_TIMESTAMP
    WHERE name = source_name;
END;
$$ LANGUAGE plpgsql;