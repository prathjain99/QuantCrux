import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  Plus, 
  Search, 
  Filter, 
  TrendingUp, 
  Activity, 
  Clock, 
  Tag,
  Play,
  Pause,
  Archive,
  Edit,
  Trash2,
  Eye
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { strategyService, Strategy, StrategyStatus, SignalType } from '../services/strategyService';
import toast from 'react-hot-toast';

const StrategiesPage: React.FC = () => {
  const { user } = useAuth();
  const [strategies, setStrategies] = useState<Strategy[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<StrategyStatus | 'ALL'>('ALL');

  useEffect(() => {
    loadStrategies();
  }, []);

  const loadStrategies = async () => {
    try {
      setLoading(true);
      const data = await strategyService.getStrategies();
      setStrategies(data);
    } catch (error: any) {
      toast.error('Failed to load strategies');
      console.error('Error loading strategies:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteStrategy = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this strategy?')) {
      return;
    }

    try {
      await strategyService.deleteStrategy(id);
      toast.success('Strategy deleted successfully');
      loadStrategies();
    } catch (error: any) {
      toast.error('Failed to delete strategy');
    }
  };

  const getStatusColor = (status: StrategyStatus) => {
    switch (status) {
      case StrategyStatus.ACTIVE:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      case StrategyStatus.DRAFT:
        return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
      case StrategyStatus.PAUSED:
        return 'text-blue-400 bg-blue-500/10 border-blue-500/20';
      case StrategyStatus.ARCHIVED:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
    }
  };

  const getStatusIcon = (status: StrategyStatus) => {
    switch (status) {
      case StrategyStatus.ACTIVE:
        return <Play className="w-3 h-3" />;
      case StrategyStatus.PAUSED:
        return <Pause className="w-3 h-3" />;
      case StrategyStatus.ARCHIVED:
        return <Archive className="w-3 h-3" />;
      default:
        return <Edit className="w-3 h-3" />;
    }
  };

  const filteredStrategies = strategies.filter(strategy => {
    const matchesSearch = strategy.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         strategy.symbol.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || strategy.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading strategies...</p>
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
                <span className="text-emerald-400 font-medium">Strategies</span>
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
            <h2 className="text-3xl font-bold text-white mb-2">Strategy Builder</h2>
            <p className="text-slate-400">Create, manage, and test your quantitative trading strategies</p>
          </div>
          
          <Link
            to="/strategies/new"
            className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 flex items-center"
          >
            <Plus className="w-5 h-5 mr-2" />
            New Strategy
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
                  placeholder="Search strategies..."
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
                  onChange={(e) => setStatusFilter(e.target.value as StrategyStatus | 'ALL')}
                  className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200 appearance-none"
                >
                  <option value="ALL">All Status</option>
                  <option value={StrategyStatus.ACTIVE}>Active</option>
                  <option value={StrategyStatus.DRAFT}>Draft</option>
                  <option value={StrategyStatus.PAUSED}>Paused</option>
                  <option value={StrategyStatus.ARCHIVED}>Archived</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Strategies Grid */}
        {filteredStrategies.length === 0 ? (
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-12 text-center">
            <Activity className="w-16 h-16 text-slate-600 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-white mb-2">No strategies found</h3>
            <p className="text-slate-400 mb-6">
              {searchTerm || statusFilter !== 'ALL' 
                ? 'Try adjusting your search or filter criteria'
                : 'Get started by creating your first quantitative trading strategy'
              }
            </p>
            {!searchTerm && statusFilter === 'ALL' && (
              <Link
                to="/strategies/new"
                className="inline-flex items-center bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105"
              >
                <Plus className="w-5 h-5 mr-2" />
                Create Strategy
              </Link>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredStrategies.map((strategy) => (
              <div
                key={strategy.id}
                className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 hover:border-emerald-500/50 transition-all duration-300 group"
              >
                {/* Strategy Header */}
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-white mb-1 group-hover:text-emerald-400 transition-colors">
                      {strategy.name}
                    </h3>
                    <p className="text-sm text-slate-400 mb-2">{strategy.symbol} • {strategy.timeframe}</p>
                    {strategy.description && (
                      <p className="text-sm text-slate-500 line-clamp-2">{strategy.description}</p>
                    )}
                  </div>
                  
                  <div className={`px-2 py-1 rounded-full border text-xs font-medium flex items-center ${getStatusColor(strategy.status)}`}>
                    {getStatusIcon(strategy.status)}
                    <span className="ml-1">{strategy.status}</span>
                  </div>
                </div>

                {/* Tags */}
                {strategy.tags && strategy.tags.length > 0 && (
                  <div className="flex flex-wrap gap-1 mb-4">
                    {strategy.tags.slice(0, 3).map((tag, index) => (
                      <span
                        key={index}
                        className="inline-flex items-center px-2 py-1 rounded-md bg-slate-800/50 text-xs text-slate-400"
                      >
                        <Tag className="w-3 h-3 mr-1" />
                        {tag}
                      </span>
                    ))}
                    {strategy.tags.length > 3 && (
                      <span className="inline-flex items-center px-2 py-1 rounded-md bg-slate-800/50 text-xs text-slate-400">
                        +{strategy.tags.length - 3} more
                      </span>
                    )}
                  </div>
                )}

                {/* Strategy Info */}
                <div className="flex items-center justify-between text-sm text-slate-400 mb-4">
                  <div className="flex items-center">
                    <Clock className="w-4 h-4 mr-1" />
                    v{strategy.currentVersion}
                  </div>
                  <div className="text-right">
                    <div>by {strategy.ownerName}</div>
                    <div className="text-xs">
                      {new Date(strategy.updatedAt).toLocaleDateString()}
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center justify-between pt-4 border-t border-slate-700">
                  <div className="flex space-x-2">
                    <Link
                      to={`/strategies/${strategy.id}`}
                      className="p-2 text-slate-400 hover:text-emerald-400 hover:bg-emerald-500/10 rounded-lg transition-all duration-200"
                      title="View Strategy"
                    >
                      <Eye className="w-4 h-4" />
                    </Link>
                    <Link
                      to={`/strategies/${strategy.id}/edit`}
                      className="p-2 text-slate-400 hover:text-blue-400 hover:bg-blue-500/10 rounded-lg transition-all duration-200"
                      title="Edit Strategy"
                    >
                      <Edit className="w-4 h-4" />
                    </Link>
                    <button
                      onClick={() => handleDeleteStrategy(strategy.id)}
                      className="p-2 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all duration-200"
                      title="Delete Strategy"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                  
                  <Link
                    to={`/strategies/${strategy.id}/test`}
                    className="bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-3 py-1 rounded-md text-sm font-medium transition-all duration-200"
                  >
                    Test Live
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

export default StrategiesPage;