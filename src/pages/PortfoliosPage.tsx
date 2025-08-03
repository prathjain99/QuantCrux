import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  Plus, 
  Search, 
  Filter, 
  TrendingUp, 
  Briefcase, 
  DollarSign,
  Activity,
  Users,
  Edit,
  Trash2,
  Eye,
  RefreshCw,
  TrendingDown,
  BarChart3,
  Clock
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { portfolioService, Portfolio, PortfolioStatus } from '../services/portfolioService';
import toast from 'react-hot-toast';

const PortfoliosPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<PortfolioStatus | 'ALL'>('ALL');

  useEffect(() => {
    loadPortfolios();
  }, []);

  const loadPortfolios = async () => {
    try {
      setLoading(true);
      const data = await portfolioService.getPortfolios();
      setPortfolios(data);
    } catch (error: any) {
      toast.error('Failed to load portfolios');
      console.error('Error loading portfolios:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeletePortfolio = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this portfolio?')) {
      return;
    }

    try {
      await portfolioService.deletePortfolio(id);
      toast.success('Portfolio deleted successfully');
      loadPortfolios();
    } catch (error: any) {
      toast.error('Failed to delete portfolio');
    }
  };

  const handleRefreshMetrics = async (id: string) => {
    try {
      await portfolioService.refreshPortfolioMetrics(id);
      toast.success('Portfolio metrics updated');
      loadPortfolios();
    } catch (error: any) {
      toast.error('Failed to refresh metrics');
    }
  };

  const getStatusColor = (status: PortfolioStatus) => {
    switch (status) {
      case PortfolioStatus.ACTIVE:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      case PortfolioStatus.SUSPENDED:
        return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
      case PortfolioStatus.ARCHIVED:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
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

  const filteredPortfolios = portfolios.filter(portfolio => {
    const matchesSearch = portfolio.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         portfolio.ownerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (portfolio.managerName && portfolio.managerName.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesStatus = statusFilter === 'ALL' || portfolio.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading portfolios...</p>
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
                <span className="text-emerald-400 font-medium">Portfolios</span>
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
            <h2 className="text-3xl font-bold text-white mb-2">Portfolio Management</h2>
            <p className="text-slate-400">Track and manage your investment portfolios</p>
          </div>
          
          <Link
            to="/portfolios/new"
            className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 flex items-center"
          >
            <Plus className="w-5 h-5 mr-2" />
            New Portfolio
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
                  placeholder="Search portfolios..."
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
                  onChange={(e) => setStatusFilter(e.target.value as PortfolioStatus | 'ALL')}
                  className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200 appearance-none"
                >
                  <option value="ALL">All Status</option>
                  <option value={PortfolioStatus.ACTIVE}>Active</option>
                  <option value={PortfolioStatus.SUSPENDED}>Suspended</option>
                  <option value={PortfolioStatus.ARCHIVED}>Archived</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Portfolios Grid */}
        {filteredPortfolios.length === 0 ? (
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-12 text-center">
            <Briefcase className="w-16 h-16 text-slate-600 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-white mb-2">No portfolios found</h3>
            <p className="text-slate-400 mb-6">
              {searchTerm || statusFilter !== 'ALL' 
                ? 'Try adjusting your search or filter criteria'
                : 'Get started by creating your first investment portfolio'
              }
            </p>
            {!searchTerm && statusFilter === 'ALL' && (
              <Link
                to="/portfolios/new"
                className="inline-flex items-center bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105"
              >
                <Plus className="w-5 h-5 mr-2" />
                Create Portfolio
              </Link>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
            {filteredPortfolios.map((portfolio) => (
              <div
                key={portfolio.id}
                className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 hover:border-emerald-500/50 transition-all duration-300 group"
              >
                {/* Portfolio Header */}
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-white mb-1 group-hover:text-emerald-400 transition-colors">
                      {portfolio.name}
                    </h3>
                    <p className="text-sm text-slate-400 mb-2">
                      Owner: {portfolio.ownerName}
                      {portfolio.managerName && (
                        <span className="ml-2">• Managed by {portfolio.managerName}</span>
                      )}
                    </p>
                    {portfolio.description && (
                      <p className="text-sm text-slate-500 line-clamp-2">{portfolio.description}</p>
                    )}
                  </div>
                  
                  <div className={`px-2 py-1 rounded-full border text-xs font-medium ${getStatusColor(portfolio.status)}`}>
                    {portfolio.status}
                  </div>
                </div>

                {/* Portfolio Metrics */}
                <div className="space-y-3 mb-4">
                  <div className="grid grid-cols-2 gap-3">
                    <div className="bg-slate-800/50 rounded-lg p-3">
                      <div className="flex items-center text-xs text-slate-400 mb-1">
                        <DollarSign className="w-3 h-3 mr-1" />
                        Current NAV
                      </div>
                      <div className="text-sm font-semibold text-white">
                        {formatCurrency(portfolio.currentNav)}
                      </div>
                    </div>
                    
                    <div className="bg-slate-800/50 rounded-lg p-3">
                      <div className="text-xs text-slate-400 mb-1">Total Return</div>
                      <div className={`text-sm font-semibold ${getReturnColor(portfolio.totalReturnPct)}`}>
                        {formatPercentage(portfolio.totalReturnPct)}
                      </div>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div className="bg-slate-800/50 rounded-lg p-3">
                      <div className="text-xs text-slate-400 mb-1">P&L</div>
                      <div className={`text-sm font-semibold ${getReturnColor(portfolio.totalPnl)}`}>
                        {formatCurrency(portfolio.totalPnl)}
                      </div>
                    </div>
                    
                    <div className="bg-slate-800/50 rounded-lg p-3">
                      <div className="text-xs text-slate-400 mb-1">Cash</div>
                      <div className="text-sm font-semibold text-blue-400">
                        {formatCurrency(portfolio.cashBalance)}
                      </div>
                    </div>
                  </div>

                  {/* Risk Metrics */}
                  {portfolio.volatility && (
                    <div className="grid grid-cols-3 gap-2 text-xs">
                      <div className="text-center">
                        <div className="text-slate-400">Volatility</div>
                        <div className="text-white font-medium">{formatPercentage(portfolio.volatility)}</div>
                      </div>
                      <div className="text-center">
                        <div className="text-slate-400">Sharpe</div>
                        <div className="text-white font-medium">{portfolio.sharpeRatio?.toFixed(2) || 'N/A'}</div>
                      </div>
                      <div className="text-center">
                        <div className="text-slate-400">Max DD</div>
                        <div className="text-red-400 font-medium">{formatPercentage(portfolio.maxDrawdown)}</div>
                      </div>
                    </div>
                  )}
                </div>

                {/* Portfolio Info */}
                <div className="flex items-center justify-between text-sm text-slate-400 mb-4">
                  <div className="flex items-center">
                    <Clock className="w-4 h-4 mr-1" />
                    {portfolio.currency} • {portfolio.benchmarkSymbol}
                  </div>
                  <div className="text-right">
                    <div className="text-xs">
                      Created {new Date(portfolio.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center justify-between pt-4 border-t border-slate-700">
                  <div className="flex space-x-2">
                    <Link
                      to={`/portfolios/${portfolio.id}`}
                      className="p-2 text-slate-400 hover:text-emerald-400 hover:bg-emerald-500/10 rounded-lg transition-all duration-200"
                      title="View Portfolio"
                    >
                      <Eye className="w-4 h-4" />
                    </Link>
                    <Link
                      to={`/portfolios/${portfolio.id}/edit`}
                      className="p-2 text-slate-400 hover:text-blue-400 hover:bg-blue-500/10 rounded-lg transition-all duration-200"
                      title="Edit Portfolio"
                    >
                      <Edit className="w-4 h-4" />
                    </Link>
                    <button
                      onClick={() => handleRefreshMetrics(portfolio.id)}
                      className="p-2 text-slate-400 hover:text-amber-400 hover:bg-amber-500/10 rounded-lg transition-all duration-200"
                      title="Refresh Metrics"
                    >
                      <RefreshCw className="w-4 h-4" />
                    </button>
                    {portfolio.status !== PortfolioStatus.ACTIVE && (
                      <button
                        onClick={() => handleDeletePortfolio(portfolio.id)}
                        className="p-2 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all duration-200"
                        title="Delete Portfolio"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                  
                  <Link
                    to={`/portfolios/${portfolio.id}`}
                    className="bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-3 py-1 rounded-md text-sm font-medium transition-all duration-200"
                  >
                    View Details
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default PortfoliosPage;