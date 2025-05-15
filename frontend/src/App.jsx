// import Home from './pages/home';

// function App() {
//   return (
//     <div>
//       <Home />
//     </div>
//   );
// }

// export default App;



// src/App.jsx

import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

// 🔐 Auth Pages
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';

// 🏠 Core Pages
import Home from './pages/home';
import Dashboard from './pages/core/Dashboard';
import StrategyBuilder from './pages/core/StrategyBuilder';
import Backtesting from './pages/core/Backtesting';
import AlphaSignals from './pages/core/AlphaSignals';
import Portfolio from './pages/core/Portfolio';
import OptionsPricing from './pages/core/OptionsPricing';
import RiskAnalytics from './pages/core/RiskAnalytics';
import MLStudio from './pages/core/MLStudio';
import StrategyLab from './pages/core/StrategyLab';
import BacktestingResult from './pages/core/BacktestingResult';
import AlphaSignalResults from './pages/core/AlphaSignalsResults';
import PortfolioOptimizerResults from './pages/core/PortfolioResults';
import RiskAnalyticsResults from './pages/core/RiskAnalyticsResults';

// 📊 Analytics Pages
import Market from './pages/analytics/Market';
import StrategyResults from './pages/analytics/StrategyResults';
import ModelComparison from './pages/analytics/ModelComparison';

// 📁 Utility Pages
import Docs from './pages/utility/Docs';
import Profile from './pages/utility/Profile';
import AdminPanel from './pages/utility/AdminPanel';
import DataImport from './pages/utility/DataImport';
import CorrelationMatrix from './pages/utility/CorrelationMatrix';
import Marketplace from './pages/utility/Marketplace';

// ⚠️ System Pages
import NotFound from './pages/system/NotFound';
import ServerError from './pages/system/ServerError';
import Unauthorized from './pages/system/Unauthorized';
import Maintenance from './pages/system/Maintenance';

function App() {
  return (
    <Router>
      <Routes>
        {/* 🔐 Auth */}
        <Route path="/log-in" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        {/* 🏠 Core */}
        <Route path="/" element={<Home />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/strategy-lab" element={<StrategyLab/>} />
        <Route path="/strategy-builder" element={<StrategyBuilder />} />
        <Route path="/backtest" element={<Backtesting />} />
        <Route path="/alpha-signals" element={<AlphaSignals />} />
        <Route path="/portfolio" element={<Portfolio />} />
        <Route path="/options-pricing" element={<OptionsPricing />} />
        <Route path="/risk-analytics" element={<RiskAnalytics />} />
        <Route path="/ml-studio" element={<MLStudio />} />
        <Route path="/backtest-result" element={<BacktestingResult />} />
        <Route path="/alpha-signals-results" element={<AlphaSignalResults />} />
        <Route path="/portfolio-results" element={<PortfolioOptimizerResults />} />
        <Route path="/risk-analytics-results" element={<RiskAnalyticsResults />} />

        {/* 📊 Analytics */}
        <Route path="/market" element={<Market />} />
        <Route path="/strategy-results" element={<StrategyResults />} />
        <Route path="/model-comparison" element={<ModelComparison />} />

        {/* 📁 Utility */}
        <Route path="/docs" element={<Docs />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/admin" element={<AdminPanel />} />
        <Route path="/data-import" element={<DataImport />} />
        <Route path="/correlation-matrix" element={<CorrelationMatrix />} />
        <Route path="/marketplace" element={<Marketplace />} />

        {/* ⚠️ System */}
        <Route path="/500" element={<ServerError />} />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="/maintenance" element={<Maintenance />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Router>
  );
}

export default App;
