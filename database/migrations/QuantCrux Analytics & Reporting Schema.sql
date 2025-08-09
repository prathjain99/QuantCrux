/*
# QuantCrux Analytics & Reporting Schema

1. New Tables
   - `analytics_risk` - Calculated risk metrics storage
   - `analytics_performance` - Performance history and metrics
   - `correlation_matrices` - Asset and strategy correlation data
   - `reports` - Generated report metadata and storage
   - `report_templates` - User-defined report templates

2. Security
   - Enable RLS on all tables
   - Role-based policies for analytics access
   - Proper indexes for performance

3. Features
   - Comprehensive risk and performance analytics
   - Correlation analysis and heatmaps
   - Custom report generation
   - Benchmark comparison capabilities
*/

-- Analytics risk metrics table
CREATE TABLE IF NOT EXISTS analytics_risk (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    strategy_id UUID REFERENCES strategies(id) ON DELETE CASCADE,
    calculation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    
    -- Risk metrics
    var_95 DECIMAL(15,2), -- Value at Risk (95%, 1-day)
    var_99 DECIMAL(15,2), -- Value at Risk (99%, 1-day)
    volatility DECIMAL(8,6), -- Annualized volatility
    beta DECIMAL(8,6), -- Beta vs benchmark
    alpha DECIMAL(8,6), -- Alpha vs benchmark
    sharpe_ratio DECIMAL(8,6), -- Sharpe ratio
    sortino_ratio DECIMAL(8,6), -- Sortino ratio
    max_drawdown DECIMAL(8,6), -- Maximum drawdown
    max_drawdown_duration INTEGER, -- Days in max drawdown
    
    -- Additional metrics
    skewness DECIMAL(8,6), -- Return distribution skewness
    kurtosis DECIMAL(8,6), -- Return distribution kurtosis
    calmar_ratio DECIMAL(8,6), -- CAGR / Max Drawdown
    information_ratio DECIMAL(8,6), -- Excess return / tracking error
    
    -- Benchmark data
    benchmark_symbol VARCHAR(20) DEFAULT 'SPY',
    correlation_to_benchmark DECIMAL(8,6),
    tracking_error DECIMAL(8,6),
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT analytics_risk_portfolio_or_strategy CHECK (
        (portfolio_id IS NOT NULL AND strategy_id IS NULL) OR
        (portfolio_id IS NULL AND strategy_id IS NOT NULL)
    )
);

-- Analytics performance metrics table
CREATE TABLE IF NOT EXISTS analytics_performance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    strategy_id UUID REFERENCES strategies(id) ON DELETE CASCADE,
    calculation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- Return metrics
    total_return DECIMAL(10,6), -- Total return for period
    cagr DECIMAL(10,6), -- Compound Annual Growth Rate
    annualized_return DECIMAL(10,6), -- Annualized return
    excess_return DECIMAL(10,6), -- Return above benchmark
    
    -- Trade metrics
    total_trades INTEGER DEFAULT 0,
    winning_trades INTEGER DEFAULT 0,
    losing_trades INTEGER DEFAULT 0,
    win_rate DECIMAL(8,6), -- Percentage of winning trades
    avg_win DECIMAL(15,2), -- Average winning trade amount
    avg_loss DECIMAL(15,2), -- Average losing trade amount
    profit_factor DECIMAL(8,6), -- Gross profit / Gross loss
    
    -- Efficiency metrics
    trade_frequency DECIMAL(8,2), -- Trades per month
    avg_holding_period INTEGER, -- Average days per position
    turnover_ratio DECIMAL(8,6), -- Portfolio turnover
    
    -- Benchmark comparison
    benchmark_return DECIMAL(10,6),
    outperformance DECIMAL(10,6), -- Return - Benchmark return
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT analytics_perf_portfolio_or_strategy CHECK (
        (portfolio_id IS NOT NULL AND strategy_id IS NULL) OR
        (portfolio_id IS NULL AND strategy_id IS NOT NULL)
    )
);

-- Correlation matrices table
CREATE TABLE IF NOT EXISTS correlation_matrices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    calculation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    period_days INTEGER NOT NULL DEFAULT 252, -- 1 year
    
    -- Correlation data stored as JSON
    asset_correlations TEXT, -- JSON matrix of asset correlations
    strategy_correlations TEXT, -- JSON matrix of strategy correlations
    mixed_correlations TEXT, -- JSON matrix of assets + strategies
    
    -- Summary statistics
    avg_correlation DECIMAL(8,6), -- Average correlation
    max_correlation DECIMAL(8,6), -- Highest correlation
    min_correlation DECIMAL(8,6), -- Lowest correlation
    diversification_ratio DECIMAL(8,6), -- Portfolio diversification measure
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(portfolio_id, calculation_date)
);

-- Reports table
CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    portfolio_id UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    strategy_id UUID REFERENCES strategies(id) ON DELETE CASCADE,
    
    -- Report metadata
    report_type VARCHAR(50) NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Date range
    period_start DATE,
    period_end DATE,
    
    -- Report configuration
    template_config TEXT, -- JSON configuration
    filters TEXT, -- JSON filters applied
    
    -- File information
    file_format VARCHAR(10) NOT NULL, -- PDF, CSV, XLS
    file_path VARCHAR(500), -- Storage path or URL
    file_size INTEGER, -- File size in bytes
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    generated_at TIMESTAMPTZ,
    downloaded_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 days'),
    
    CONSTRAINT valid_report_type CHECK (report_type IN (
        'PORTFOLIO_SUMMARY', 'RISK_ANALYSIS', 'PERFORMANCE_REPORT', 
        'TRADE_BLOTTER', 'ATTRIBUTION_ANALYSIS', 'CORRELATION_REPORT', 'CUSTOM'
    )),
    CONSTRAINT valid_file_format CHECK (file_format IN ('PDF', 'CSV', 'XLS')),
    CONSTRAINT valid_status CHECK (status IN ('PENDING', 'GENERATING', 'COMPLETED', 'FAILED', 'EXPIRED'))
);

-- Report templates table
CREATE TABLE IF NOT EXISTS report_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    report_type VARCHAR(50) NOT NULL,
    
    -- Template configuration
    template_config TEXT NOT NULL, -- JSON template definition
    default_filters TEXT, -- JSON default filters
    
    -- Sharing settings
    is_public BOOLEAN DEFAULT FALSE,
    is_system_template BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_template_report_type CHECK (report_type IN (
        'PORTFOLIO_SUMMARY', 'RISK_ANALYSIS', 'PERFORMANCE_REPORT', 
        'TRADE_BLOTTER', 'ATTRIBUTION_ANALYSIS', 'CORRELATION_REPORT', 'CUSTOM'
    ))
);

-- Attribution analysis table
CREATE TABLE IF NOT EXISTS attribution_analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    calculation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- Attribution breakdown stored as JSON
    asset_attribution TEXT, -- JSON: {"AAPL": 0.45, "MSFT": 0.30, ...}
    sector_attribution TEXT, -- JSON: {"Technology": 0.60, "Finance": 0.25, ...}
    strategy_attribution TEXT, -- JSON: {"RSI_Strategy": 0.35, "MACD_Strategy": 0.20, ...}
    product_attribution TEXT, -- JSON: {"Digital_Option_1": 0.15, ...}
    
    -- Summary metrics
    total_return DECIMAL(10,6),
    active_return DECIMAL(10,6), -- Return above benchmark
    selection_effect DECIMAL(10,6), -- Stock selection contribution
    allocation_effect DECIMAL(10,6), -- Asset allocation contribution
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(portfolio_id, calculation_date)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_analytics_risk_portfolio_id ON analytics_risk(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_analytics_risk_strategy_id ON analytics_risk(strategy_id);
CREATE INDEX IF NOT EXISTS idx_analytics_risk_calculation_date ON analytics_risk(calculation_date);

CREATE INDEX IF NOT EXISTS idx_analytics_perf_portfolio_id ON analytics_performance(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_analytics_perf_strategy_id ON analytics_performance(strategy_id);
CREATE INDEX IF NOT EXISTS idx_analytics_perf_calculation_date ON analytics_performance(calculation_date);

CREATE INDEX IF NOT EXISTS idx_correlation_matrices_portfolio_id ON correlation_matrices(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_correlation_matrices_calculation_date ON correlation_matrices(calculation_date);

CREATE INDEX IF NOT EXISTS idx_reports_user_id ON reports(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_portfolio_id ON reports(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_reports_strategy_id ON reports(strategy_id);
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
CREATE INDEX IF NOT EXISTS idx_reports_created_at ON reports(created_at);

CREATE INDEX IF NOT EXISTS idx_report_templates_user_id ON report_templates(user_id);
CREATE INDEX IF NOT EXISTS idx_report_templates_report_type ON report_templates(report_type);

CREATE INDEX IF NOT EXISTS idx_attribution_analysis_portfolio_id ON attribution_analysis(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_attribution_analysis_calculation_date ON attribution_analysis(calculation_date);

-- Enable Row Level Security
ALTER TABLE analytics_risk ENABLE ROW LEVEL SECURITY;
ALTER TABLE analytics_performance ENABLE ROW LEVEL SECURITY;
ALTER TABLE correlation_matrices ENABLE ROW LEVEL SECURITY;
ALTER TABLE reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE report_templates ENABLE ROW LEVEL SECURITY;
ALTER TABLE attribution_analysis ENABLE ROW LEVEL SECURITY;

-- RLS Policies for analytics_risk
CREATE POLICY "Users can read own portfolio risk analytics" ON analytics_risk
    FOR SELECT USING (
        (portfolio_id IS NOT NULL AND EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = analytics_risk.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )) OR
        (strategy_id IS NOT NULL AND EXISTS (
            SELECT 1 FROM strategies s 
            WHERE s.id = analytics_risk.strategy_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        ))
    );

CREATE POLICY "System can manage risk analytics" ON analytics_risk
    FOR ALL USING (true);

-- RLS Policies for analytics_performance
CREATE POLICY "Users can read own performance analytics" ON analytics_performance
    FOR SELECT USING (
        (portfolio_id IS NOT NULL AND EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = analytics_performance.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )) OR
        (strategy_id IS NOT NULL AND EXISTS (
            SELECT 1 FROM strategies s 
            WHERE s.id = analytics_performance.strategy_id 
            AND s.user_id = current_setting('app.current_user_id', true)::UUID
        ))
    );

CREATE POLICY "System can manage performance analytics" ON analytics_performance
    FOR ALL USING (true);

-- RLS Policies for correlation_matrices
CREATE POLICY "Users can read own correlation matrices" ON correlation_matrices
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = correlation_matrices.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )
    );

CREATE POLICY "System can manage correlation matrices" ON correlation_matrices
    FOR ALL USING (true);

-- RLS Policies for reports
CREATE POLICY "Users can read own reports" ON reports
    FOR SELECT USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can create reports" ON reports
    FOR INSERT WITH CHECK (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can update own reports" ON reports
    FOR UPDATE USING (user_id = current_setting('app.current_user_id', true)::UUID);

CREATE POLICY "Users can delete own reports" ON reports
    FOR DELETE USING (user_id = current_setting('app.current_user_id', true)::UUID);

-- RLS Policies for report_templates
CREATE POLICY "Users can read own templates" ON report_templates
    FOR SELECT USING (
        user_id = current_setting('app.current_user_id', true)::UUID OR
        is_public = true OR
        is_system_template = true
    );

CREATE POLICY "Users can manage own templates" ON report_templates
    FOR ALL USING (user_id = current_setting('app.current_user_id', true)::UUID);

-- RLS Policies for attribution_analysis
CREATE POLICY "Users can read own attribution analysis" ON attribution_analysis
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM portfolios p 
            WHERE p.id = attribution_analysis.portfolio_id 
            AND (p.owner_id = current_setting('app.current_user_id', true)::UUID OR
                 p.manager_id = current_setting('app.current_user_id', true)::UUID)
        )
    );

CREATE POLICY "System can manage attribution analysis" ON attribution_analysis
    FOR ALL USING (true);

-- Update timestamp triggers
CREATE TRIGGER update_analytics_risk_updated_at BEFORE UPDATE ON analytics_risk 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_report_templates_updated_at BEFORE UPDATE ON report_templates 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed sample analytics data
INSERT INTO analytics_risk (portfolio_id, var_95, var_99, volatility, beta, alpha, sharpe_ratio, max_drawdown, benchmark_symbol, correlation_to_benchmark)
SELECT 
    p.id,
    -2500.00, -- $2,500 VaR (95%)
    -3800.00, -- $3,800 VaR (99%)
    0.18, -- 18% volatility
    0.92, -- Beta of 0.92
    0.024, -- 2.4% alpha
    1.34, -- Sharpe ratio of 1.34
    -0.085, -- 8.5% max drawdown
    'SPY',
    0.75 -- 75% correlation to SPY
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO analytics_performance (portfolio_id, period_start, period_end, total_return, cagr, total_trades, winning_trades, losing_trades, win_rate, profit_factor, benchmark_return, outperformance)
SELECT 
    p.id,
    CURRENT_DATE - INTERVAL '30 days',
    CURRENT_DATE,
    0.025, -- 2.5% return
    0.172, -- 17.2% CAGR
    15, -- 15 trades
    10, -- 10 winning trades
    5, -- 5 losing trades
    0.667, -- 66.7% win rate
    1.85, -- Profit factor
    0.018, -- 1.8% benchmark return
    0.007 -- 0.7% outperformance
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Seed sample correlation matrix
INSERT INTO correlation_matrices (portfolio_id, asset_correlations, avg_correlation, max_correlation, min_correlation, diversification_ratio)
SELECT 
    p.id,
    '{"AAPL": {"MSFT": 0.65, "GOOGL": 0.72, "TSLA": 0.45}, "MSFT": {"GOOGL": 0.68, "TSLA": 0.38}, "GOOGL": {"TSLA": 0.42}}',
    0.55, -- Average correlation
    0.72, -- Max correlation (AAPL-GOOGL)
    0.38, -- Min correlation (MSFT-TSLA)
    0.78 -- Diversification ratio
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Seed sample attribution analysis
INSERT INTO attribution_analysis (portfolio_id, period_start, period_end, asset_attribution, sector_attribution, total_return, active_return)
SELECT 
    p.id,
    CURRENT_DATE - INTERVAL '30 days',
    CURRENT_DATE,
    '{"AAPL": 0.45, "MSFT": 0.30, "GOOGL": 0.15, "Cash": 0.10}',
    '{"Technology": 0.75, "Cash": 0.25}',
    0.025, -- 2.5% total return
    0.007 -- 0.7% active return
FROM portfolios p
WHERE p.name = 'Growth Portfolio 2025'
LIMIT 1
ON CONFLICT DO NOTHING;

-- Seed system report templates
INSERT INTO report_templates (user_id, name, description, report_type, template_config, is_system_template)
SELECT 
    u.id,
    'Monthly Portfolio Summary',
    'Standard monthly portfolio performance and risk report',
    'PORTFOLIO_SUMMARY',
    '{"sections": ["overview", "performance", "risk", "holdings", "transactions"], "charts": ["nav_curve", "allocation_pie"], "period": "monthly"}',
    true
FROM users u
WHERE u.role = 'ADMIN'
LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO report_templates (user_id, name, description, report_type, template_config, is_system_template)
SELECT 
    u.id,
    'Risk Analysis Report',
    'Comprehensive risk metrics and correlation analysis',
    'RISK_ANALYSIS',
    '{"sections": ["risk_metrics", "var_analysis", "correlation_matrix", "stress_tests"], "charts": ["risk_evolution", "correlation_heatmap"], "period": "quarterly"}',
    true
FROM users u
WHERE u.role = 'ADMIN'
LIMIT 1
ON CONFLICT DO NOTHING;