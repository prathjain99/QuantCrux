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

        <Route path="/500" element={<ServerError />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="/maintenance" element={<Maintenance />} />
        <Route path="*" element={<NotFound />} />

        
        

        {/* You can add more routes later */}
      </Routes>
    </BrowserRouter>  
  </React.StrictMode>
)
