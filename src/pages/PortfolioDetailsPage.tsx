import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { 
  TrendingUp, 
  ArrowLeft, 
  RefreshCw, 
  Edit,
  DollarSign,
  Activity,
  BarChart3,
  Users,
  AlertTriangle,
  TrendingDown,
  Clock,
  Target
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { useAuth } from '../hooks/useAuth';
import { portfolioService, Portfolio } from '../services/portfolioService';
import toast from 'react-hot-toast';

const PortfolioDetailsPage: React.FC = () => {
  const { user } = useAuth();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [portfolio, setPortfolio] = useState<Portfolio | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'overview' | 'holdings' | 'transactions' | 'analytics'>('overview');

  useEffect(() => {
    if (id) {
      loadPortfolio();
    } else {
      setError('No portfolio ID provided');
      setLoading(false);
    }
  }, [id]);

  const loadPortfolio = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(null);
      const data = await portfolioService.getPortfolio(id);
      setPortfolio(data);
    } catch (error: any) {
      console.error('Failed to load portfolio:', error);
      setError('Failed to load portfolio details');
      toast.error('Failed to load portfolio details');
    } finally {
      setLoading(false);
    }
  };

  const handleRefreshMetrics = async () => {
    if (!portfolio) return;
    
    try {
      const updatedPortfolio = await portfolioService.refreshPortfolioMetrics(portfolio.id);
      setPortfolio(updatedPortfolio);
      toast.success('Portfolio metrics updated');
    } catch (error: any) {
      toast.error('Failed to refresh metrics');
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

  const getReturnColor = (value?: number) => {
    if (value === undefined || value === null) return 'text-slate-400';
    return value >= 0 ? 'text-emerald-400' : 'text-red-400';
  };

  // Prepare chart data
  const navChartData = portfolio?.navHistory?.map(point => ({
    date: new Date(point.date).toLocaleDateString(),
    nav: point.nav,
    return: point.dailyReturn ? point.dailyReturn * 100 : 0
  })) || [];

  const pieColors = ['#10B981', '#3B82F6', '#8B5CF6', '#F59E0B', '#EF4444', '#6B7280'];

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading portfolio details...</p>
        </div>
      </div>
    );
  }

  if (error || !portfolio) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <AlertTriangle className="w-16 h-16 text-red-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-white mb-2">
            {error || 'Portfolio Not Found'}
          </h2>
          <p className="text-slate-400 mb-6">
            {error || 'The requested portfolio could not be found.'}
          </p>
          <Link
            to="/portfolios"
            className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200"
          >
            Back to Portfolios
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
                <Link to="/portfolios" className="text-slate-400 hover:text-white transition-colors flex items-center">
                  <ArrowLeft className="w-4 h-4 mr-1" />
                  Portfolios
                </Link>
                <span className="text-slate-600">/</span>
                <span className="text-emerald-400 font-medium">Details</span>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <button 
                onClick={handleRefreshMetrics}
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                title="Refresh Metrics"
              >
                <RefreshCw className="w-5 h-5" />
              </button>
              <Link
                to={`/portfolios/${portfolio.id}/edit`}
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                title="Edit Portfolio"
              >
                <Edit className="w-5 h-5" />
              </Link>
              <span className="text-sm text-slate-400">{user?.fullName}</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Portfolio Header */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 mb-8">
          <div className="flex justify-between items-start">
            <div>
              <h2 className="text-2xl font-bold text-white mb-2">{portfolio.name}</h2>
              <div className="flex items-center space-x-6 text-sm text-slate-400">
                <div className="flex items-center">
                  <Users className="w-4 h-4 mr-1" />
                  Owner: {portfolio.ownerName}
                </div>
                {portfolio.managerName && (
                  <div className="flex items-center">
                    <Activity className="w-4 h-4 mr-1" />
                    Manager: {portfolio.managerName}
                  </div>
                )}
                <div className="flex items-center">
                  <BarChart3 className="w-4 h-4 mr-1" />
                  Benchmark: {portfolio.benchmarkSymbol}
                </div>
              </div>
              {portfolio.description && (
                <p className="text-slate-300 mt-2">{portfolio.description}</p>
              )}
            </div>
            
            <div className="text-right">
              <div className="text-sm text-slate-400 mb-1">Status</div>
              <div className={`px-3 py-1 rounded-full text-sm font-medium ${
                portfolio.status === 'ACTIVE' 
                  ? 'bg-emerald-500/20 text-emerald-400'
                  : 'bg-slate-500/20 text-slate-400'
              }`}>
                {portfolio.status}
              </div>
            </div>
          </div>
        </div>

        {/* Key Metrics */}
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4 mb-8">
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="flex items-center text-slate-400 text-sm mb-2">
              <DollarSign className="w-4 h-4 mr-1" />
              Current NAV
            </div>
            <div className="text-xl font-bold text-white">
              {formatCurrency(portfolio.currentNav)}
            </div>
            <div className={`text-sm ${getReturnColor(portfolio.totalReturnPct)}`}>
              {formatPercentage(portfolio.totalReturnPct)}
            </div>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="text-slate-400 text-sm mb-2">Total P&L</div>
            <div className={`text-xl font-bold ${getReturnColor(portfolio.totalPnl)}`}>
              {formatCurrency(portfolio.totalPnl)}
            </div>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="text-slate-400 text-sm mb-2">Cash Balance</div>
            <div className="text-xl font-bold text-blue-400">
              {formatCurrency(portfolio.cashBalance)}
            </div>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="text-slate-400 text-sm mb-2">Volatility</div>
            <div className="text-xl font-bold text-purple-400">
              {formatPercentage(portfolio.volatility)}
            </div>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="flex items-center text-slate-400 text-sm mb-2">
              <TrendingDown className="w-4 h-4 mr-1" />
              Max Drawdown
            </div>
            <div className="text-xl font-bold text-red-400">
              {formatPercentage(portfolio.maxDrawdown)}
            </div>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="text-slate-400 text-sm mb-2">Sharpe Ratio</div>
            <div className="text-xl font-bold text-white">
              {portfolio.sharpeRatio?.toFixed(2) || 'N/A'}
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 mb-8">
          <div className="border-b border-slate-700">
            <nav className="flex space-x-8 px-6">
              {[
                { id: 'overview', label: 'Overview', icon: BarChart3 },
                { id: 'holdings', label: 'Holdings', icon: Activity },
                { id: 'transactions', label: 'Transactions', icon: Clock },
                { id: 'analytics', label: 'Risk Analytics', icon: TrendingUp }
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
                {/* NAV Chart */}
                {navChartData.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">NAV Performance</h3>
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <ResponsiveContainer width="100%" height={300}>
                        <LineChart data={navChartData}>
                          <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                          <XAxis 
                            dataKey="date" 
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
                            formatter={(value: any) => [formatCurrency(value), 'NAV']}
                          />
                          <Line
                            type="monotone"
                            dataKey="nav"
                            stroke="#10B981"
                            strokeWidth={2}
                            dot={false}
                          />
                        </LineChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                )}

                {/* Allocation Charts */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                  {/* Asset Allocation */}
                  {portfolio.assetAllocation && portfolio.assetAllocation.length > 0 && (
                    <div>
                      <h3 className="text-lg font-semibold text-white mb-4">Asset Allocation</h3>
                      <div className="bg-slate-800/50 rounded-lg p-4">
                        <ResponsiveContainer width="100%" height={250}>
                          <PieChart>
                            <Pie
                              data={portfolio.assetAllocation}
                              cx="50%"
                              cy="50%"
                              outerRadius={80}
                              dataKey="value"
                              nameKey="category"
                            >
                              {portfolio.assetAllocation.map((_, index) => (
                                <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                              ))}
                            </Pie>
                            <Tooltip 
                              formatter={(value: any) => [formatCurrency(value), 'Value']}
                              contentStyle={{
                                backgroundColor: '#1F2937',
                                border: '1px solid #374151',
                                borderRadius: '8px',
                                color: '#F9FAFB'
                              }}
                            />
                          </PieChart>
                        </ResponsiveContainer>
                      </div>
                    </div>
                  )}

                  {/* Sector Allocation */}
                  {portfolio.sectorAllocation && portfolio.sectorAllocation.length > 0 && (
                    <div>
                      <h3 className="text-lg font-semibold text-white mb-4">Sector Allocation</h3>
                      <div className="bg-slate-800/50 rounded-lg p-4">
                        <ResponsiveContainer width="100%" height={250}>
                          <PieChart>
                            <Pie
                              data={portfolio.sectorAllocation}
                              cx="50%"
                              cy="50%"
                              outerRadius={80}
                              dataKey="value"
                              nameKey="category"
                            >
                              {portfolio.sectorAllocation.map((_, index) => (
                                <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                              ))}
                            </Pie>
                            <Tooltip 
                              formatter={(value: any) => [formatCurrency(value), 'Value']}
                              contentStyle={{
                                backgroundColor: '#1F2937',
                                border: '1px solid #374151',
                                borderRadius: '8px',
                                color: '#F9FAFB'
                              }}
                            />
                          </PieChart>
                        </ResponsiveContainer>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            {activeTab === 'holdings' && (
              <div>
                <h3 className="text-lg font-semibold text-white mb-4">Current Holdings</h3>
                {portfolio.holdings && portfolio.holdings.length > 0 ? (
                  <div className="bg-slate-800/50 rounded-lg overflow-hidden">
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead className="bg-slate-700/50">
                          <tr>
                            <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Symbol</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Type</th>
                            <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase tracking-wider">Quantity</th>
                            <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase tracking-wider">Avg Price</th>
                            <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase tracking-wider">Market Value</th>
                            <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase tracking-wider">P&L</th>
                            <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase tracking-wider">Weight</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-700">
                          {portfolio.holdings.map((holding) => (
                            <tr key={holding.id} className="hover:bg-slate-700/30">
                              <td className="px-4 py-3">
                                <div className="text-sm font-medium text-white">{holding.symbol}</div>
                                {holding.sector && (
                                  <div className="text-xs text-slate-400">{holding.sector}</div>
                                )}
                              </td>
                              <td className="px-4 py-3">
                                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-500/20 text-blue-400">
                                  {holding.instrumentType}
                                </span>
                              </td>
                              <td className="px-4 py-3 text-right text-sm text-white">
                                {holding.quantity.toLocaleString()}
                              </td>
                              <td className="px-4 py-3 text-right text-sm text-white">
                                {formatCurrency(holding.avgPrice)}
                              </td>
                              <td className="px-4 py-3 text-right text-sm text-white">
                                {formatCurrency(holding.marketValue)}
                              </td>
                              <td className={`px-4 py-3 text-right text-sm font-medium ${getReturnColor(holding.unrealizedPnl)}`}>
                                {formatCurrency(holding.unrealizedPnl)}
                              </td>
                              <td className="px-4 py-3 text-right text-sm text-slate-300">
                                {holding.weightPct?.toFixed(1)}%
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                ) : (
                  <div className="bg-slate-800/50 rounded-lg p-8 text-center">
                    <Activity className="w-12 h-12 text-slate-600 mx-auto mb-4" />
                    <p className="text-slate-400">No holdings in this portfolio</p>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'transactions' && (
              <div>
                <h3 className="text-lg font-semibold text-white mb-4">Recent Transactions</h3>
                {portfolio.recentTransactions && portfolio.recentTransactions.length > 0 ? (
                  <div className="bg-slate-800/50 rounded-lg overflow-hidden">
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead className="bg-slate-700/50">
                          <tr>
                            <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Date</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Type</th>
                            <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase tracking-wider">Symbol</th>
                            <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase tracking-wider">Quantity</th>
                            <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase tracking-wider">Price</th>
                            <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase tracking-wider">Amount</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-700">
                          {portfolio.recentTransactions.map((transaction) => (
                            <tr key={transaction.id} className="hover:bg-slate-700/30">
                              <td className="px-4 py-3 text-sm text-slate-300">
                                {new Date(transaction.executedAt).toLocaleDateString()}
                              </td>
                              <td className="px-4 py-3">
                                <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                                  transaction.transactionType === 'BUY' ? 'bg-emerald-500/20 text-emerald-400' :
                                  transaction.transactionType === 'SELL' ? 'bg-red-500/20 text-red-400' :
                                  'bg-blue-500/20 text-blue-400'
                                }`}>
                                  {transaction.transactionType}
                                </span>
                              </td>
                              <td className="px-4 py-3 text-sm text-white">
                                {transaction.symbol || '-'}
                              </td>
                              <td className="px-4 py-3 text-right text-sm text-white">
                                {transaction.quantity?.toLocaleString() || '-'}
                              </td>
                              <td className="px-4 py-3 text-right text-sm text-white">
                                {transaction.price ? formatCurrency(transaction.price) : '-'}
                              </td>
                              <td className={`px-4 py-3 text-right text-sm font-medium ${
                                transaction.amount >= 0 ? 'text-emerald-400' : 'text-red-400'
                              }`}>
                                {formatCurrency(Math.abs(transaction.amount))}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                ) : (
                  <div className="bg-slate-800/50 rounded-lg p-8 text-center">
                    <Clock className="w-12 h-12 text-slate-600 mx-auto mb-4" />
                    <p className="text-slate-400">No transactions found</p>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'analytics' && (
              <div className="space-y-6">
                {/* Risk Summary */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Risk Summary</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <h4 className="text-white font-medium mb-3">Risk Metrics</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-slate-400">Value at Risk (95%)</span>
                          <span className="text-red-400">{formatCurrency(portfolio.var95)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-slate-400">Volatility (Annualized)</span>
                          <span className="text-white">{formatPercentage(portfolio.volatility)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-slate-400">Beta</span>
                          <span className="text-white">{portfolio.beta?.toFixed(2) || 'N/A'}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-slate-400">Maximum Drawdown</span>
                          <span className="text-red-400">{formatPercentage(portfolio.maxDrawdown)}</span>
                        </div>
                      </div>
                    </div>

                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <h4 className="text-white font-medium mb-3">Performance Metrics</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-slate-400">Total Return</span>
                          <span className={getReturnColor(portfolio.totalReturnPct)}>
                            {formatPercentage(portfolio.totalReturnPct)}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-slate-400">Sharpe Ratio</span>
                          <span className="text-white">{portfolio.sharpeRatio?.toFixed(2) || 'N/A'}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-slate-400">Initial Capital</span>
                          <span className="text-white">{formatCurrency(portfolio.initialCapital)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-slate-400">Current NAV</span>
                          <span className="text-emerald-400">{formatCurrency(portfolio.currentNav)}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Portfolio Composition */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Portfolio Composition</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Asset Class Breakdown */}
                    {portfolio.assetAllocation && portfolio.assetAllocation.length > 0 && (
                      <div className="bg-slate-800/50 rounded-lg p-4">
                        <h4 className="text-white font-medium mb-3">By Asset Class</h4>
                        <div className="space-y-2">
                          {portfolio.assetAllocation.map((allocation, index) => (
                            <div key={allocation.category} className="flex items-center justify-between">
                              <div className="flex items-center">
                                <div 
                                  className="w-3 h-3 rounded-full mr-2"
                                  style={{ backgroundColor: pieColors[index % pieColors.length] }}
                                ></div>
                                <span className="text-sm text-slate-300">{allocation.category}</span>
                              </div>
                              <div className="text-right">
                                <div className="text-sm text-white">{allocation.percentage.toFixed(1)}%</div>
                                <div className="text-xs text-slate-400">{formatCurrency(allocation.value)}</div>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Sector Breakdown */}
                    {portfolio.sectorAllocation && portfolio.sectorAllocation.length > 0 && (
                      <div className="bg-slate-800/50 rounded-lg p-4">
                        <h4 className="text-white font-medium mb-3">By Sector</h4>
                        <div className="space-y-2">
                          {portfolio.sectorAllocation.map((allocation, index) => (
                            <div key={allocation.category} className="flex items-center justify-between">
                              <div className="flex items-center">
                                <div 
                                  className="w-3 h-3 rounded-full mr-2"
                                  style={{ backgroundColor: pieColors[index % pieColors.length] }}
                                ></div>
                                <span className="text-sm text-slate-300">{allocation.category}</span>
                              </div>
                              <div className="text-right">
                                <div className="text-sm text-white">{allocation.percentage.toFixed(1)}%</div>
                                <div className="text-xs text-slate-400">{formatCurrency(allocation.value)}</div>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
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

export default PortfolioDetailsPage;