import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import Home from './pages/home'

// 🔐 Auth Pages
import Register from './pages/auth/Register';
import Login from './pages/auth/Login';
import ForgotPassword from './pages/auth/ForgotPassword';


// ⚠️ System Pages
import NotFound from './pages/system/NotFound';
import ServerError from './pages/system/ServerError';
import Unauthorized from './pages/system/Unauthorized';
import Maintenance from './pages/system/Maintenance';

import BacktestingEngine from './pages/core/Backtesting';
import BacktestingResult from './pages/core/BacktestingResult';
import AlphaSignal from './pages/core/AlphaSignals';
import AlphaSignalResults from './pages/core/AlphaSignalsResults';
import PortfolioOptimizer from './pages/core/Portfolio';
import PortfolioOptimizerResults from './pages/core/PortfolioResults';
import RiskAnalytics from './pages/core/RiskAnalytics';
import RiskAnalyticsResults from './pages/core/RiskAnalyticsResults';
import OptionsPricing from './pages/core/OptionsPricing';
import OptionsPricingResults from './pages/core/OptionsPricingResults';
import RegimeDetection from './pages/core/RegimeDetection';
import RegimeDetectionResults from './pages/core/RegimeDetectionResults';
import Dashboard from './pages/core/Dashboard';
import SessionDetails from './pages/utility/SessionDetails';
import SessionCompare from './pages/analytics/ModelComparison';






import './index.css'


ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/register" element={<Register />} />
        <Route path="/log-in" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        <Route path="/backtest" element={<BacktestingEngine />} />
        <Route path="/backtest-result" element={<BacktestingResult />} />
        <Route path="/alpha-signals" element={<AlphaSignal />} />
        <Route path="/alpha-signals-results" element={<AlphaSignalResults />} />
        <Route path="/portfolio" element={<PortfolioOptimizer />} />
        <Route path="/portfolio-results" element={<PortfolioOptimizerResults />} />
        <Route path="/risk-analytics" element={<RiskAnalytics />} />
        <Route path="/risk-analytics-results" element={<RiskAnalyticsResults />} />
        <Route path="/options-pricing" element={<OptionsPricing />} />
        <Route path="/options-pricing-results" element={<OptionsPricingResults />} />
        <Route path="/regime-detection" element={<RegimeDetection />} />
        <Route path="/regime-detection-results" element={<RegimeDetectionResults />} />
        <Route path="dashboard/session-details" element={<SessionDetails />} />

        <Route path="/dashboard" element={<Dashboard />} />
        
        <Route path="dashboard/model-comparison" element={<SessionCompare />} />

        <Route path="/500" element={<ServerError />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="/maintenance" element={<Maintenance />} />
        <Route path="*" element={<NotFound />} />

        
        

        {/* You can add more routes later */}
      </Routes>
    </BrowserRouter>  
  </React.StrictMode>
)
