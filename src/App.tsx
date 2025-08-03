import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { useAuth } from './hooks/useAuth';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import Dashboard from './pages/Dashboard';
import StrategiesPage from './pages/StrategiesPage';
import StrategyBuilderPage from './pages/StrategyBuilderPage';
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