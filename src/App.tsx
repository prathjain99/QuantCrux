import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { useAuth } from './hooks/useAuth';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import Dashboard from './pages/Dashboard';
import StrategiesPage from './pages/StrategiesPage';
import StrategyBuilderPage from './pages/StrategyBuilderPage';
import BacktestsPage from './pages/BacktestsPage';
import BacktestResultsPage from './pages/BacktestResultsPage';
import ProductsPage from './pages/ProductsPage';
//import ProductBuilderPage from './pages/ProductBuilderPage';
import ProductDetailsPage from './pages/ProductDetailsPage';
import ProductBuilderPage from './pages/ProductBuilderPage';
import PortfoliosPage from './pages/PortfoliosPage';
import PortfolioDetailsPage from './pages/PortfolioDetailsPage';
import PortfolioBuilderPage from './pages/PortfolioBuilderPage';
import TradeDeskPage from './pages/TradeDeskPage';
import AnalyticsPage from './pages/AnalyticsPage';
import MarketDataPage from './pages/MarketDataPage';
import LoadingSpinner from './components/LoadingSpinner';

function AppRoutes() {
  const { user, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <Routes>
      <Route 
        path="/login" 
        element={user ? <Navigate to="/dashboard" /> : <LoginPage />} 
      />
      <Route 
        path="/register" 
        element={user ? <Navigate to="/dashboard" /> : <RegisterPage />} 
      />
      <Route 
        path="/dashboard" 
        element={user ? <Dashboard /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/strategies" 
        element={user ? <StrategiesPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/strategies/new" 
        element={user ? <StrategyBuilderPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/strategies/:id/edit" 
        element={user ? <StrategyBuilderPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/backtests" 
        element={user ? <BacktestsPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/backtests/:id" 
        element={user ? <BacktestResultsPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/products" 
        element={user ? <ProductsPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/products/new" 
        element={user ? <ProductBuilderPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/products/:id/edit" 
        element={user ? <ProductBuilderPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/products/:id" 
        element={user ? <ProductDetailsPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/portfolios" 
        element={user ? <PortfoliosPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/portfolios/new" 
        element={user ? <PortfolioBuilderPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/portfolios/:id/edit" 
        element={user ? <PortfolioBuilderPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/portfolios/:id" 
        element={user ? <PortfolioDetailsPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/trade-desk" 
        element={user ? <TradeDeskPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/analytics" 
        element={user ? <AnalyticsPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/market-data" 
        element={user ? <MarketDataPage /> : <Navigate to="/login" />} 
      />
      <Route 
        path="/" 
        element={<Navigate to={user ? "/dashboard" : "/login"} />} 
      />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-slate-950">
          <AppRoutes />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;