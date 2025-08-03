import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  Plus, 
  Search, 
  Filter, 
  TrendingUp, 
  Activity, 
  Clock, 
  Play,
  CheckCircle,
  XCircle,
  AlertCircle,
  Pause,
  Eye,
  Trash2,
  BarChart3,
  Calendar,
  DollarSign
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { backtestService, Backtest, BacktestStatus } from '../services/backtestService';
import toast from 'react-hot-toast';

const BacktestsPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [backtests, setBacktests] = useState<Backtest[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<BacktestStatus | 'ALL'>('ALL');

  useEffect(() => {
    loadBacktests();
    
    // Poll for running backtests
    const interval = setInterval(() => {
      const runningBacktests = backtests.filter(b => b.status === BacktestStatus.RUNNING);
      if (runningBacktests.length > 0) {
        loadBacktests();
      }
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const loadBacktests = async () => {
    try {
      setLoading(true);
      const data = await backtestService.getBacktests();
      setBacktests(data);
    } catch (error: any) {
      toast.error('Failed to load backtests');
      console.error('Error loading backtests:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteBacktest = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this backtest?')) {
      return;
    }

    try {
      await backtestService.deleteBacktest(id);
      toast.success('Backtest deleted successfully');
      loadBacktests();
    } catch (error: any) {
      toast.error('Failed to delete backtest');
    }
  };

  const getStatusColor = (status: BacktestStatus) => {
    switch (status) {
      case BacktestStatus.COMPLETED:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      case BacktestStatus.RUNNING:
        return 'text-blue-400 bg-blue-500/10 border-blue-500/20';
      case BacktestStatus.PENDING:
        return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
      case BacktestStatus.FAILED:
        return 'text-red-400 bg-red-500/10 border-red-500/20';
      case BacktestStatus.CANCELLED:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
    }
  };

  const getStatusIcon = (status: BacktestStatus) => {
    switch (status) {
      case BacktestStatus.COMPLETED:
        return <CheckCircle className="w-3 h-3" />;
      case BacktestStatus.RUNNING:
        return <Play className="w-3 h-3" />;
      case BacktestStatus.PENDING:
        return <Clock className="w-3 h-3" />;
      case BacktestStatus.FAILED:
        return <XCircle className="w-3 h-3" />;
      case BacktestStatus.CANCELLED:
        return <Pause className="w-3 h-3" />;
      default:
        return <AlertCircle className="w-3 h-3" />;
    }
  };

  const formatCurrency = (value?: number) => {
    if (value === undefined || value === null) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  const formatPercentage = (value?: number) => {
    if (value === undefined || value === null) return 'N/A';
    const percentage = value * 100;
    return `${percentage >= 0 ? '+' : ''}${percentage.toFixed(2)}%`;
  };

  const formatMetric = (value?: number, decimals: number = 2) => {
    if (value === undefined || value === null) return 'N/A';
    return value.toFixed(decimals);
  };

  const filteredBacktests = backtests.filter(backtest => {
    const matchesSearch = backtest.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         backtest.strategyName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         backtest.symbol.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || backtest.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading backtests...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
      {/* Header */}
      <header className="bg-slate-900/50 backdrop-blur-sm border-b border-slate-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <Link to="/dashboard" className="flex items-center mr-8">
                <TrendingUp className="w-8 h-8 text-emerald-400 mr-3" />
                <h1 className="text-xl font-bold bg-gradient-to-r from-emerald-400 to-blue-400 bg-clip-text text-transparent">
                  QuantCrux
                </h1>
              </Link>
              <nav className="flex space-x-8">
                <Link to="/dashboard" className="text-slate-400 hover:text-white transition-colors">
                  Dashboard
                </Link>
                <Link to="/strategies" className="text-slate-400 hover:text-white transition-colors">
                  Strategies
                </Link>
                <span className="text-emerald-400 font-medium">Backtests</span>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <span className="text-sm text-slate-400">{user?.fullName}</span>
              <div className="w-8 h-8 bg-emerald-500/20 rounded-full flex items-center justify-center">
                <span className="text-emerald-400 text-sm font-medium">
                  {user?.fullName?.charAt(0)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h2 className="text-3xl font-bold text-white mb-2">Backtesting Engine</h2>
            <p className="text-slate-400">Validate your strategies with historical market data</p>
          </div>
          
          <Link
            to="/strategies"
            className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 flex items-center"
          >
            <Plus className="w-5 h-5 mr-2" />
            New Backtest
          </Link>
        </div>

        {/* Filters */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 mb-8">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  type="text"
                  placeholder="Search backtests..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                />
              </div>
            </div>
            
            <div className="sm:w-48">
              <div className="relative">
                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value as BacktestStatus | 'ALL')}
                  className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200 appearance-none"
                >
                  <option value="ALL">All Status</option>
                  <option value={BacktestStatus.COMPLETED}>Completed</option>
                  <option value={BacktestStatus.RUNNING}>Running</option>
                  <option value={BacktestStatus.PENDING}>Pending</option>
                  <option value={BacktestStatus.FAILED}>Failed</option>
                  <option value={BacktestStatus.CANCELLED}>Cancelled</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Backtests Grid */}
        {filteredBacktests.length === 0 ? (
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-12 text-center">
            <BarChart3 className="w-16 h-16 text-slate-600 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-white mb-2">No backtests found</h3>
            <p className="text-slate-400 mb-6">
              {searchTerm || statusFilter !== 'ALL' 
                ? 'Try adjusting your search or filter criteria'
                : 'Get started by running your first backtest on a strategy'
              }
            </p>
            {!searchTerm && statusFilter === 'ALL' && (
              <Link
                to="/strategies"
                className="inline-flex items-center bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105"
              >
                <Plus className="w-5 h-5 mr-2" />
                Run Backtest
              </Link>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
            {filteredBacktests.map((backtest) => (
              <div
                key={backtest.id}
                className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 hover:border-emerald-500/50 transition-all duration-300 group"
              >
                {/* Backtest Header */}
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-white mb-1 group-hover:text-emerald-400 transition-colors">
                      {backtest.name}
                    </h3>
                    <p className="text-sm text-slate-400 mb-2">
                      {backtest.strategyName} • {backtest.symbol} • {backtest.timeframe}
                    </p>
                    <div className="flex items-center text-xs text-slate-500">
                      <Calendar className="w-3 h-3 mr-1" />
                      {new Date(backtest.startDate).toLocaleDateString()} - {new Date(backtest.endDate).toLocaleDateString()}
                    </div>
                  </div>
                  
                  <div className={`px-2 py-1 rounded-full border text-xs font-medium flex items-center ${getStatusColor(backtest.status)}`}>
                    {getStatusIcon(backtest.status)}
                    <span className="ml-1">{backtest.status}</span>
                  </div>
                </div>

                {/* Progress Bar for Running Backtests */}
                {backtest.status === BacktestStatus.RUNNING && (
                  <div className="mb-4">
                    <div className="flex justify-between text-xs text-slate-400 mb-1">
                      <span>Progress</span>
                      <span>{backtest.progress}%</span>
                    </div>
                    <div className="w-full bg-slate-700 rounded-full h-2">
                      <div 
                        className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${backtest.progress}%` }}
                      ></div>
                    </div>
                  </div>
                )}

                {/* Error Message */}
                {backtest.status === BacktestStatus.FAILED && backtest.errorMessage && (
                  <div className="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg">
                    <p className="text-xs text-red-400">{backtest.errorMessage}</p>
                  </div>
                )}

                {/* Results Summary */}
                {backtest.status === BacktestStatus.COMPLETED && (
                  <div className="space-y-3 mb-4">
                    <div className="grid grid-cols-2 gap-3">
                      <div className="bg-slate-800/50 rounded-lg p-3">
                        <div className="flex items-center text-xs text-slate-400 mb-1">
                          <DollarSign className="w-3 h-3 mr-1" />
                          Final Capital
                        </div>
                        <div className="text-sm font-semibold text-white">
                          {formatCurrency(backtest.finalCapital)}
                        </div>
                      </div>
                      
                      <div className="bg-slate-800/50 rounded-lg p-3">
                        <div className="text-xs text-slate-400 mb-1">Total Return</div>
                        <div className={`text-sm font-semibold ${
                          (backtest.totalReturn || 0) >= 0 ? 'text-emerald-400' : 'text-red-400'
                        }`}>
                          {formatPercentage(backtest.totalReturn)}
                        </div>
                      </div>
                    </div>

                    <div className="grid grid-cols-3 gap-2 text-xs">
                      <div className="text-center">
                        <div className="text-slate-400">Sharpe</div>
                        <div className="text-white font-medium">{formatMetric(backtest.sharpeRatio)}</div>
                      </div>
                      <div className="text-center">
                        <div className="text-slate-400">Max DD</div>
                        <div className="text-red-400 font-medium">{formatPercentage(backtest.maxDrawdown)}</div>
                      </div>
                      <div className="text-center">
                        <div className="text-slate-400">Win Rate</div>
                        <div className="text-emerald-400 font-medium">{formatPercentage(backtest.winRate)}</div>
                      </div>
                    </div>

                    <div className="text-xs text-slate-400 text-center">
                      {backtest.totalTrades} trades • {backtest.winningTrades} wins • {backtest.losingTrades} losses
                    </div>
                  </div>
                )}

                {/* Backtest Info */}
                <div className="flex items-center justify-between text-sm text-slate-400 mb-4">
                  <div className="flex items-center">
                    <Clock className="w-4 h-4 mr-1" />
                    {formatCurrency(backtest.initialCapital)}
                  </div>
                  <div className="text-right">
                    <div>by {backtest.ownerName}</div>
                    <div className="text-xs">
                      {new Date(backtest.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center justify-between pt-4 border-t border-slate-700">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => navigate(`/backtests/${backtest.id}`)}
                      className="p-2 text-slate-400 hover:text-emerald-400 hover:bg-emerald-500/10 rounded-lg transition-all duration-200"
                      title="View Results"
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDeleteBacktest(backtest.id)}
                      className="p-2 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all duration-200"
                      title="Delete Backtest"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                  
                  {backtest.status === BacktestStatus.COMPLETED && (
                    <button
                      onClick={() => navigate(`/backtests/${backtest.id}`)}
                      className="bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-3 py-1 rounded-md text-sm font-medium transition-all duration-200"
                    >
                      View Results
                    </button>
                  )}
                  
                  {backtest.status === BacktestStatus.RUNNING && (
                    <div className="bg-blue-500/20 text-blue-400 px-3 py-1 rounded-md text-sm font-medium">
                      Running...
                    </div>
                  )}
                  
                  {backtest.status === BacktestStatus.PENDING && (
                    <div className="bg-amber-500/20 text-amber-400 px-3 py-1 rounded-md text-sm font-medium">
                      Queued
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default BacktestsPage;