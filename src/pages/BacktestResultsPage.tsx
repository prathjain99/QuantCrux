import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { 
  TrendingUp, 
  ArrowLeft, 
  Download, 
  Share2,
  Calendar,
  DollarSign,
  TrendingDown,
  Activity,
  Target,
  Clock,
  BarChart3,
  AlertTriangle
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import { useAuth } from '../hooks/useAuth';
import { backtestService, Backtest, BacktestStatus } from '../services/backtestService';
import toast from 'react-hot-toast';

const BacktestResultsPage: React.FC = () => {
  const { user } = useAuth();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  // Always call hooks in the same order
  const [backtest, setBacktest] = useState<Backtest | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'overview' | 'trades' | 'analysis'>('overview');
  const [error, setError] = useState<string | null>(null);

  // Always call useEffect hooks
  useEffect(() => {
    if (id) {
      loadBacktest();
    } else {
      setError('No backtest ID provided');
      setLoading(false);
    }
  }, [id]);

  // Polling effect - always called but conditionally active
  useEffect(() => {
    if (backtest?.status === BacktestStatus.RUNNING) {
      const interval = setInterval(() => {
        loadBacktest();
      }, 3000);
      
      return () => clearInterval(interval);
    }
  }, [backtest?.status]);

  const loadBacktest = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(null);
      const data = await backtestService.getBacktest(id);
      setBacktest(data);
    } catch (error: any) {
      console.error('Failed to load backtest:', error);
      setError('Failed to load backtest results');
      toast.error('Failed to load backtest results');
    } finally {
      setLoading(false);
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

  const getReturnColor = (value?: number) => {
    if (value === undefined || value === null) return 'text-slate-400';
    return value >= 0 ? 'text-emerald-400' : 'text-red-400';
  };

  // Prepare chart data - always called but may be empty
  const equityChartData = backtest?.equityCurve?.map(point => ({
    timestamp: new Date(point.timestamp).toLocaleDateString(),
    equity: point.equity,
    return: ((point.equity - (backtest.initialCapital || 100000)) / (backtest.initialCapital || 100000)) * 100
  })) || [];

  const drawdownChartData = backtest?.drawdownCurve?.map(point => ({
    timestamp: new Date(point.timestamp).toLocaleDateString(),
    drawdown: point.drawdown * 100
  })) || [];

  // Loading state
  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading backtest results...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (error || !backtest) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <AlertTriangle className="w-16 h-16 text-red-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-white mb-2">
            {error || 'Backtest Not Found'}
          </h2>
          <p className="text-slate-400 mb-6">
            {error || 'The requested backtest could not be found.'}
          </p>
          <Link
            to="/backtests"
            className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200"
          >
            Back to Backtests
          </Link>
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
              <nav className="flex items-center space-x-4">
                <Link to="/backtests" className="text-slate-400 hover:text-white transition-colors flex items-center">
                  <ArrowLeft className="w-4 h-4 mr-1" />
                  Backtests
                </Link>
                <span className="text-slate-600">/</span>
                <span className="text-emerald-400 font-medium">Results</span>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <button className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200">
                <Download className="w-5 h-5" />
              </button>
              <button className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200">
                <Share2 className="w-5 h-5" />
              </button>
              <span className="text-sm text-slate-400">{user?.fullName}</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Backtest Header */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 mb-8">
          <div className="flex justify-between items-start">
            <div>
              <h2 className="text-2xl font-bold text-white mb-2">{backtest.name}</h2>
              <div className="flex items-center space-x-6 text-sm text-slate-400">
                <div className="flex items-center">
                  <Activity className="w-4 h-4 mr-1" />
                  {backtest.strategyName}
                </div>
                <div className="flex items-center">
                  <BarChart3 className="w-4 h-4 mr-1" />
                  {backtest.symbol} • {backtest.timeframe}
                </div>
                <div className="flex items-center">
                  <Calendar className="w-4 h-4 mr-1" />
                  {new Date(backtest.startDate).toLocaleDateString()} - {new Date(backtest.endDate).toLocaleDateString()}
                </div>
              </div>
            </div>
            
            <div className="text-right">
              <div className="text-sm text-slate-400 mb-1">Status</div>
              <div className={`px-3 py-1 rounded-full text-sm font-medium ${
                backtest.status === BacktestStatus.COMPLETED 
                  ? 'bg-emerald-500/20 text-emerald-400'
                  : backtest.status === BacktestStatus.RUNNING
                  ? 'bg-blue-500/20 text-blue-400'
                  : backtest.status === BacktestStatus.FAILED
                  ? 'bg-red-500/20 text-red-400'
                  : 'bg-slate-500/20 text-slate-400'
              }`}>
                {backtest.status}
              </div>
            </div>
          </div>

          {/* Progress Bar for Running Backtests */}
          {backtest.status === BacktestStatus.RUNNING && (
            <div className="mt-4">
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
            <div className="mt-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg">
              <p className="text-sm text-red-400">{backtest.errorMessage}</p>
            </div>
          )}
        </div>

        {/* Key Metrics */}
        {backtest.status === BacktestStatus.COMPLETED && (
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4 mb-8">
            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="flex items-center text-slate-400 text-sm mb-2">
                <DollarSign className="w-4 h-4 mr-1" />
                Final Capital
              </div>
              <div className="text-xl font-bold text-white">
                {formatCurrency(backtest.finalCapital)}
              </div>
              <div className={`text-sm ${getReturnColor(backtest.totalReturn)}`}>
                {formatPercentage(backtest.totalReturn)}
              </div>
            </div>

            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="text-slate-400 text-sm mb-2">Sharpe Ratio</div>
              <div className="text-xl font-bold text-white">
                {formatMetric(backtest.sharpeRatio)}
              </div>
              <div className="text-sm text-slate-500">Risk-adjusted</div>
            </div>

            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="flex items-center text-slate-400 text-sm mb-2">
                <TrendingDown className="w-4 h-4 mr-1" />
                Max Drawdown
              </div>
              <div className="text-xl font-bold text-red-400">
                {formatPercentage(backtest.maxDrawdown)}
              </div>
              <div className="text-sm text-slate-500">Peak to trough</div>
            </div>

            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="flex items-center text-slate-400 text-sm mb-2">
                <Target className="w-4 h-4 mr-1" />
                Win Rate
              </div>
              <div className="text-xl font-bold text-emerald-400">
                {formatPercentage(backtest.winRate)}
              </div>
              <div className="text-sm text-slate-500">
                {backtest.winningTrades}/{backtest.totalTrades} trades
              </div>
            </div>

            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="text-slate-400 text-sm mb-2">CAGR</div>
              <div className={`text-xl font-bold ${getReturnColor(backtest.cagr)}`}>
                {formatPercentage(backtest.cagr)}
              </div>
              <div className="text-sm text-slate-500">Annualized</div>
            </div>

            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="text-slate-400 text-sm mb-2">Profit Factor</div>
              <div className="text-xl font-bold text-white">
                {formatMetric(backtest.profitFactor)}
              </div>
              <div className="text-sm text-slate-500">Gross profit/loss</div>
            </div>
          </div>
        )}

        {/* Tabs */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 mb-8">
          <div className="border-b border-slate-700">
            <nav className="flex space-x-8 px-6">
              {[
                { id: 'overview', label: 'Overview', icon: BarChart3 },
                { id: 'trades', label: 'Trade Log', icon: Activity },
                { id: 'analysis', label: 'Analysis', icon: TrendingUp }
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as any)}
                  className={`flex items-center py-4 px-2 border-b-2 font-medium text-sm transition-colors ${
                    activeTab === tab.id
                      ? 'border-emerald-500 text-emerald-400'
                      : 'border-transparent text-slate-400 hover:text-slate-300'
                  }`}
                >
                  <tab.icon className="w-4 h-4 mr-2" />
                  {tab.label}
                </button>
              ))}
            </nav>
          </div>

          <div className="p-6">
            {activeTab === 'overview' && (
              <div className="space-y-8">
                {/* Equity Curve */}
                {equityChartData.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">Equity Curve</h3>
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={equityChartData}>
                          <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                          <XAxis 
                            dataKey="timestamp" 
                            stroke="#9CA3AF"
                            fontSize={12}
                          />
                          <YAxis 
                            stroke="#9CA3AF"
                            fontSize={12}
                            tickFormatter={(value) => formatCurrency(value)}
                          />
                          <Tooltip 
                            contentStyle={{
                              backgroundColor: '#1F2937',
                              border: '1px solid #374151',
                              borderRadius: '8px',
                              color: '#F9FAFB'
                            }}
                            formatter={(value: any) => [formatCurrency(value), 'Equity']}
                          />
                          <Area
                            type="monotone"
                            dataKey="equity"
                            stroke="#10B981"
                            fill="#10B981"
                            fillOpacity={0.1}
                            strokeWidth={2}
                          />
                        </AreaChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                )}

                {/* Drawdown Chart */}
                {drawdownChartData.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">Drawdown</h3>
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <ResponsiveContainer width="100%" height={200}>
                        <AreaChart data={drawdownChartData}>
                          <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                          <XAxis 
                            dataKey="timestamp" 
                            stroke="#9CA3AF"
                            fontSize={12}
                          />
                          <YAxis 
                            stroke="#9CA3AF"
                            fontSize={12}
                            tickFormatter={(value) => `${value.toFixed(1)}%`}
                          />
                          <Tooltip 
                            contentStyle={{
                              backgroundColor: '#1F2937',
                              border: '1px solid #374151',
                              borderRadius: '8px',
                              color: '#F9FAFB'
                            }}
                            formatter={(value: any) => [`${value.toFixed(2)}%`, 'Drawdown']}
                          />
                          <Area
                            type="monotone"
                            dataKey="drawdown"
                            stroke="#EF4444"
                            fill="#EF4444"
                            fillOpacity={0.1}
                            strokeWidth={2}
                          />
                        </AreaChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                )}

                {/* No Data Message */}
                {equityChartData.length === 0 && drawdownChartData.length === 0 && (
                  <div className="text-center py-8">
                    <BarChart3 className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                    <p className="text-slate-400">No chart data available for this backtest</p>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'trades' && (
              <div>
                <h3 className="text-lg font-semibold text-white mb-4">Trade Log</h3>
                <div className="bg-slate-800/50 rounded-lg p-4">
                  <p className="text-slate-400 text-center py-8">
                    Trade log details will be displayed here. This feature shows individual trade entries, exits, and P&L.
                  </p>
                </div>
              </div>
            )}

            {activeTab === 'analysis' && (
              <div>
                <h3 className="text-lg font-semibold text-white mb-4">Advanced Analysis</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="bg-slate-800/50 rounded-lg p-4">
                    <h4 className="text-white font-medium mb-3">Risk Metrics</h4>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-slate-400">Volatility</span>
                        <span className="text-white">{formatPercentage(backtest.volatility)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Sortino Ratio</span>
                        <span className="text-white">{formatMetric(backtest.sortinoRatio)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Max DD Duration</span>
                        <span className="text-white">{backtest.maxDrawdownDuration || 'N/A'} days</span>
                      </div>
                    </div>
                  </div>

                  <div className="bg-slate-800/50 rounded-lg p-4">
                    <h4 className="text-white font-medium mb-3">Trade Statistics</h4>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-slate-400">Total Trades</span>
                        <span className="text-white">{backtest.totalTrades}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Avg Trade Duration</span>
                        <span className="text-white">{backtest.avgTradeDuration || 'N/A'} min</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-400">Losing Trades</span>
                        <span className="text-white">{backtest.losingTrades}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default BacktestResultsPage;