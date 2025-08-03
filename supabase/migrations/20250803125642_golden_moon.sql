/*
# QuantCrux Product Builder Schema

1. New Tables
   - `products` - Core structured product definitions
   - `product_versions` - Version control for product modifications
   - `product_pricings` - Historical pricing and Greeks data
   - `product_payoffs` - Payoff curve data points for visualization

2. Security
   - Enable RLS on all tables
   - Role-based policies for PORTFOLIO_MANAGER access
   - Proper indexes for performance

3. Features
   - Comprehensive product lifecycle management
   - Advanced pricing models (Monte Carlo, Black-Scholes)
   - Greeks calculation and risk analytics
   - Strategy linking for performance-based products
*/

-- Products table - Core structured product definitions
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    product_type VARCHAR(50) NOT NULL,
    underlying_asset VARCHAR(50) NOT NULL,
    linked_strategy_id UUID REFERENCES strategies(id),
    
    -- Product terms
    notional DECIMAL(15,2) NOT NULL DEFAULT 100000.00,
    strike_price DECIMAL(15,6),
    barrier_level DECIMAL(15,6),
    payoff_rate DECIMAL(8,6),
    
    -- Dates
    issue_date DATE,
    maturity_date DATE NOT NULL,
    settlement_date DATE,
    
    -- Configuration
    config_json TEXT NOT NULL,
    pricing_model VARCHAR(50) NOT NULL DEFAULT 'MONTE_CARLO',
    
    -- Pricing results
    fair_value DECIMAL(15,2),
    implied_volatility DECIMAL(8,6),
    
    -- Greeks
    delta_value DECIMAL(10,6),
    gamma_value DECIMAL(10,6),
    theta_value DECIMAL(10,6),
    vega_value DECIMAL(10,6),
    rho_value DECIMAL(10,6),
    
    -- Status and metadata
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    current_version INTEGER DEFAULT 1,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    issued_at TIMESTAMPTZ,
    
    CONSTRAINT valid_product_type CHECK (product_type IN (
        'DIGITAL_OPTION', 'BARRIER_OPTION', 'KNOCK_IN_OPTION', 
        'KNOCK_OUT_OPTION', 'DUAL_CURRENCY', 'STRATEGY_LINKED_NOTE', 'CUSTOM_PAYOFF'
    )),
    CONSTRAINT valid_status CHECK (status IN ('DRAFT', 'ISSUED', 'ACTIVE', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT valid_pricing_model CHECK (pricing_model IN ('BLACK_SCHOLES', 'MONTE_CARLO', 'BINOMIAL_TREE', 'CUSTOM')),
    CONSTRAINT valid_dates CHECK (maturity_date > COALESCE(issue_date, CURRENT_DATE))
);

-- Product versions for version control
CREATE TABLE IF NOT EXISTS product_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    config_json TEXT NOT NULL,
    change_description TEXT,
    author_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(product_id, version_number)
);

-- Product pricing history
CREATE TABLE IF NOT EXISTS product_pricings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    pricing_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fair_value DECIMAL(15,2) NOT NULL,
    implied_volatility DECIMAL(8,6),
    
    -- Greeks at pricing time
    delta_value DECIMAL(10,6),
    gamma_value DECIMAL(10,6),
    theta_value DECIMAL(10,6),
    vega_value DECIMAL(10,6),
    rho_value DECIMAL(10,6),
    
    -- Market conditions
    underlying_price DECIMAL(15,6),
    risk_free_rate DECIMAL(8,6),
    time_to_maturity DECIMAL(10,6),
    
    -- Pricing parameters
    simulation_runs INTEGER,
    pricing_model VARCHAR(50),
    model_parameters TEXT, -- JSON
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Product payoff curves for visualization
CREATE TABLE IF NOT EXISTS product_payoffs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    spot_price DECIMAL(15,6) NOT NULL,
    payoff_value DECIMAL(15,6) NOT NULL,
    probability DECIMAL(8,6),
    scenario_type VARCHAR(50), -- 'base', 'stress', 'monte_carlo'
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_products_user_id ON products(user_id);
CREATE INDEX IF NOT EXISTS idx_products_type ON products(product_type);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_underlying ON products(underlying_asset);
CREATE INDEX IF NOT EXISTS idx_products_strategy ON products(linked_strategy_id);
CREATE INDEX IF NOT EXISTS idx_products_maturity ON products(maturity_date);

CREATE INDEX IF NOT EXISTS idx_product_versions_product_id ON product_versions(product_id);
CREATE INDEX IF NOT EXISTS idx_product_pricings_product_id ON product_pricings(product_id);
CREATE INDEX IF NOT EXISTS idx_product_pricings_date ON product_pricings(pricing_date);
CREATE INDEX IF NOT EXISTS idx_product_payoffs_product_id ON product_payoffs(product_id);

-- Enable Row Level Security
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_versions ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_pricings ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_payoffs ENABLE ROW LEVEL SECURITY;

-- RLS Policies for products
CREATE POLICY "Users can read own products" ON products
    FOR SELECT USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Portfolio managers can create products" ON products
    FOR INSERT WITH CHECK (
        user_id = current_setting('app.current_user_id', true)::UUID AND
        EXISTS (
            SELECT 1 FROM users u 
            WHERE u.id = current_setting('app.current_user_id', true)::UUID 
            AND u.role IN ('PORTFOLIO_MANAGER', 'ADMIN')
        )
    );

CREATE POLICY "Users can update own products" ON products
    FOR UPDATE USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can delete own products" ON products
    FOR DELETE USING (user_id = current_setting('app.current_user_id', true)::UUID);

-- RLS Policies for product_versions
CREATE POLICY "Users can read own product versions" ON product_versions
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM products p 
            WHERE p.id = product_versions.product_id 
            AND p.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "Users can create product versions" ON product_versions
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM products p 
            WHERE p.id = product_versions.product_id 
            AND p.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

-- RLS Policies for product_pricings
CREATE POLICY "Users can read own product pricings" ON product_pricings
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM products p 
            WHERE p.id = product_pricings.product_id 
            AND p.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "System can manage product pricings" ON product_pricings
    FOR ALL USING (true);

-- RLS Policies for product_payoffs
CREATE POLICY "Users can read own product payoffs" ON product_payoffs
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM products p 
            WHERE p.id = product_payoffs.product_id 
            AND p.user_id = current_setting('app.current_user_id', true)::UUID
        )
    );

CREATE POLICY "System can manage product payoffs" ON product_payoffs
    FOR ALL USING (true);

-- Update timestamp trigger for products
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed sample products for testing
INSERT INTO products (user_id, name, description, product_type, underlying_asset, notional, strike_price, payoff_rate, maturity_date, config_json, status) 
SELECT 
    u.id,
    'AAPL Digital Option',
    'Digital option paying 12% if AAPL strategy returns > 5%',
    'DIGITAL_OPTION',
    'AAPL',
    100000.00,
    150.00,
    0.12,
    '2025-12-31',
    '{"condition":"strategy_return > 5%","payoff":"12%","barrier_monitoring":"continuous","settlement":"cash"}',
    'DRAFT'
FROM users u 
WHERE u.role IN ('PORTFOLIO_MANAGER', 'ADMIN')
LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO products (user_id, name, description, product_type, underlying_asset, notional, barrier_level, payoff_rate, maturity_date, config_json, status) 
SELECT 
    u.id,
    'BTC Barrier Option',
    'Knock-in barrier option on Bitcoin with 15% upside participation',
    'BARRIER_OPTION',
    'BTCUSD',
    50000.00,
    40000.00,
    0.15,
    '2025-09-30',
    '{"barrier_type":"knock_in","barrier_level":40000,"participation_rate":"15%","protection":"100%"}',
    'ISSUED'
FROM users u 
WHERE u.role IN ('PORTFOLIO_MANAGER', 'ADMIN')
LIMIT 1
ON CONFLICT DO NOTHING;

-- Insert corresponding product versions
INSERT INTO product_versions (product_id, version_number, config_json, change_description, author_id)
SELECT 
    p.id,
    1,
    p.config_json,
    'Initial version',
    p.user_id
FROM products p
ON CONFLICT DO NOTHING;