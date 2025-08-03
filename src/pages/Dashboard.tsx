import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { UserRole } from '../services/authService';
import { 
  TrendingUp, 
  LogOut, 
  User, 
  Shield, 
  BarChart3, 
  Briefcase,
  Activity,
  Clock,
  Users,
  Settings,
  Lock
} from 'lucide-react';

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();

  const getRoleColor = (role: UserRole) => {
    switch (role) {
      case UserRole.ADMIN:
        return 'text-red-400 bg-red-500/10 border-red-500/20';
      case UserRole.PORTFOLIO_MANAGER:
        return 'text-blue-400 bg-blue-500/10 border-blue-500/20';
      case UserRole.RESEARCHER:
        return 'text-purple-400 bg-purple-500/10 border-purple-500/20';
      case UserRole.CLIENT:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
    }
  };

  const getRoleDescription = (role: UserRole) => {
    switch (role) {
      case UserRole.ADMIN:
        return 'System Administrator - Full platform access';
      case UserRole.PORTFOLIO_MANAGER:
        return 'Portfolio Manager - Client and strategy management';
      case UserRole.RESEARCHER:
        return 'Researcher - Strategy development and analysis';
      case UserRole.CLIENT:
        return 'Client - Portfolio and trading access';
      default:
        return 'User';
    }
  };

  const getModuleAccess = (role: UserRole) => {
    const allModules = [
      { name: 'Strategy Builder', icon: Activity, description: 'Create and manage quantitative strategies', path: '/strategies' },
      { name: 'Backtesting', icon: BarChart3, description: 'Historical strategy simulation and analysis', path: '/backtests' },
      { name: 'Product Builder', icon: Briefcase, description: 'Structured product creation and pricing', path: '/products' },
      { name: 'Product Builder', icon: Briefcase, description: 'Structured product creation and pricing', path: '/products' },
      { name: 'Trade Desk', icon: TrendingUp, description: 'Order execution and trade management' },
      { name: 'Portfolio Management', icon: Users, description: 'Portfolio tracking and analytics' },
      { name: 'Risk Analytics', icon: Shield, description: 'Risk metrics and performance analysis' },
      { name: 'User Management', icon: Settings, description: 'User administration and permissions' }
    ];

    const roleAccess = {
      [UserRole.ADMIN]: allModules,
      [UserRole.PORTFOLIO_MANAGER]: allModules.filter(m => m.name !== 'User Management'),
      [UserRole.RESEARCHER]: allModules.filter(m => !['Trade Desk', 'Portfolio Management', 'User Management'].includes(m.name)),
      [UserRole.CLIENT]: allModules.filter(m => !['Product Builder', 'User Management'].includes(m.name))
    };

    return roleAccess[role] || [];
  };

  const accessibleModules = getModuleAccess(user?.role || UserRole.CLIENT);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
      {/* Header */}
      <header className="bg-slate-900/50 backdrop-blur-sm border-b border-slate-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <TrendingUp className="w-8 h-8 text-emerald-400 mr-3" />
              <h1 className="text-xl font-bold bg-gradient-to-r from-emerald-400 to-blue-400 bg-clip-text text-transparent">
                QuantCrux
              </h1>
            </div>
            
            <div className="flex items-center space-x-4">
              <div className="text-right">
                <p className="text-sm text-white font-medium">{user?.fullName}</p>
                <p className="text-xs text-slate-400">{user?.email}</p>
              </div>
              
              <div className={`px-3 py-1 rounded-full border text-xs font-medium ${getRoleColor(user?.role || UserRole.CLIENT)}`}>
                {user?.role}
              </div>
              
              <button
                onClick={logout}
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                title="Logout"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Section */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-2xl border border-slate-700 p-8 mb-8">
          <div className="flex items-start justify-between">
            <div>
              <h2 className="text-3xl font-bold text-white mb-2">
                Welcome back, {user?.fullName?.split(' ')[0]}
              </h2>
              <p className="text-slate-400 mb-4">
                {getRoleDescription(user?.role || UserRole.CLIENT)}
              </p>
              <div className="flex items-center space-x-6 text-sm text-slate-400">
                <div className="flex items-center">
                  <User className="w-4 h-4 mr-1" />
                  @{user?.username}
                </div>
                <div className="flex items-center">
                  <Clock className="w-4 h-4 mr-1" />
                  Member since 2024
                </div>
              </div>
            </div>
            
            <div className="text-right">
              <div className="text-4xl font-bold text-emerald-400 mb-1">Live</div>
              <div className="text-sm text-slate-400">System Status</div>
            </div>
          </div>
        </div>

        {/* Module Access Grid */}
        <div className="mb-8">
          <h3 className="text-xl font-semibold text-white mb-6">Available Modules</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {accessibleModules.map((module, index) => (
              module.path ? (
                <Link
                  to={module.path}
                  key={index}
                  className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 hover:border-emerald-500/50 transition-all duration-300 cursor-pointer group block"
                >
                  <div className="flex items-start space-x-4">
                    <div className="p-3 bg-emerald-500/10 rounded-lg group-hover:bg-emerald-500/20 transition-colors">
                      <module.icon className="w-6 h-6 text-emerald-400" />
                    </div>
                    <div className="flex-1">
                      <h4 className="text-lg font-semibold text-white mb-2 group-hover:text-emerald-400 transition-colors">
                        {module.name}
                      </h4>
                      <p className="text-sm text-slate-400 leading-relaxed">
                        {module.description}
                      </p>
                    </div>
                  </div>
                  
                  <div className="mt-4 flex justify-end">
                    <span className="text-xs text-emerald-400 font-medium">Available</span>
                  </div>
                </Link>
              ) : (
                <div
                  key={index}
                  className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 hover:border-emerald-500/50 transition-all duration-300 cursor-pointer group"
                >
                  <div className="flex items-start space-x-4">
                    <div className="p-3 bg-emerald-500/10 rounded-lg group-hover:bg-emerald-500/20 transition-colors">
                      <module.icon className="w-6 h-6 text-emerald-400" />
                    </div>
                    <div className="flex-1">
                      <h4 className="text-lg font-semibold text-white mb-2 group-hover:text-emerald-400 transition-colors">
                        {module.name}
                      </h4>
                      <p className="text-sm text-slate-400 leading-relaxed">
                        {module.description}
                      </p>
                    </div>
                  </div>
                  
                  <div className="mt-4 flex justify-end">
                    <span className="text-xs text-emerald-400 font-medium">Coming Soon</span>
                  </div>
                </div>
              )
            ))}
          </div>
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-400">Account Status</p>
                <p className="text-2xl font-bold text-emerald-400">Active</p>
              </div>
              <Shield className="w-8 h-8 text-emerald-400" />
            </div>
          </div>
          
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-400">Accessible Modules</p>
                <p className="text-2xl font-bold text-blue-400">{accessibleModules.length}</p>
              </div>
              <Activity className="w-8 h-8 text-blue-400" />
            </div>
          </div>
          
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-slate-400">Session Security</p>
                <p className="text-2xl font-bold text-amber-400">Secure</p>
              </div>
              <Lock className="w-8 h-8 text-amber-400" />
            </div>
          </div>
        </div>

        {/* Next Steps */}
        <div className="mt-8 bg-slate-900/30 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
          <h3 className="text-lg font-semibold text-white mb-4">Module Status</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div className="space-y-2">
              <p className="text-emerald-400 font-medium">✅ Completed Modules:</p>
              <ul className="text-slate-400 space-y-1 list-disc list-inside">
                <li>Authentication & User Management</li>
                <li>Strategy Builder - Create & test strategies</li>
              </ul>
            </div>
            <div className="space-y-2">
              <p className="text-blue-400 font-medium">🔄 Coming Next:</p>
              <ul className="text-slate-400 space-y-1 list-disc list-inside">
                <li>Backtesting Engine - Historical simulations</li>
                <li>Product Builder - Structured products</li>
                <li>Trade Desk - Order execution</li>
                <li>Portfolio Management - Track holdings & P&L</li>
              </ul>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;