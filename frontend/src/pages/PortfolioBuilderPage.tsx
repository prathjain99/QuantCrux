import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { 
  Save, 
  TrendingUp, 
  ArrowLeft, 
  Briefcase, 
  DollarSign,
  Users,
  Settings,
  AlertCircle
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { portfolioService, PortfolioRequest, PortfolioStatus } from '../services/portfolioService';
import { UserRole } from '../services/authService';
import toast from 'react-hot-toast';

const PortfolioBuilderPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = !!id;

  const [formData, setFormData] = useState<PortfolioRequest>({
    name: '',
    description: '',
    initialCapital: 100000,
    status: PortfolioStatus.ACTIVE,
    currency: 'USD',
    benchmarkSymbol: 'SPY'
  });

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isEditing) {
      loadPortfolio();
    }
  }, [id]);

  const loadPortfolio = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      const portfolio = await portfolioService.getPortfolio(id);
      setFormData({
        name: portfolio.name,
        description: portfolio.description || '',
        initialCapital: portfolio.initialCapital,
        managerId: portfolio.managerId || undefined,
        status: portfolio.status,
        currency: portfolio.currency,
        benchmarkSymbol: portfolio.benchmarkSymbol
      });
    } catch (error: any) {
      toast.error('Failed to load portfolio');
      navigate('/portfolios');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'initialCapital' ? parseFloat(value) || 0 : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      
      if (isEditing && id) {
        await portfolioService.updatePortfolio(id, formData);
        toast.success('Portfolio updated successfully');
      } else {
        await portfolioService.createPortfolio(formData);
        toast.success('Portfolio created successfully');
      }
      
      navigate('/portfolios');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to save portfolio');
    } finally {
      setLoading(false);
    }
  };

  if (loading && isEditing) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading portfolio...</p>
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
                <span className="text-emerald-400 font-medium">
                  {isEditing ? 'Edit Portfolio' : 'New Portfolio'}
                </span>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <span className="text-sm text-slate-400">{user?.fullName}</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-8">
          <div className="flex items-center mb-8">
            <Briefcase className="w-8 h-8 text-emerald-400 mr-3" />
            <div>
              <h2 className="text-2xl font-semibold text-white">
                {isEditing ? 'Edit Portfolio' : 'Create New Portfolio'}
              </h2>
              <p className="text-slate-400 mt-1">
                {isEditing ? 'Update portfolio settings and configuration' : 'Set up a new investment portfolio to track your holdings'}
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-8">
            {/* Basic Information */}
            <div>
              <h3 className="text-lg font-medium text-white mb-4 flex items-center">
                <Settings className="w-5 h-5 mr-2 text-emerald-400" />
                Basic Information
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Portfolio Name
                  </label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    placeholder="Enter portfolio name"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Status
                  </label>
                  <select
                    name="status"
                    value={formData.status}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  >
                    <option value={PortfolioStatus.ACTIVE}>Active</option>
                    <option value={PortfolioStatus.SUSPENDED}>Suspended</option>
                    <option value={PortfolioStatus.ARCHIVED}>Archived</option>
                  </select>
                </div>
              </div>

              <div className="mt-6">
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Description
                </label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  rows={3}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  placeholder="Describe your portfolio strategy and objectives..."
                />
              </div>
            </div>

            {/* Financial Settings */}
            <div>
              <h3 className="text-lg font-medium text-white mb-4 flex items-center">
                <DollarSign className="w-5 h-5 mr-2 text-blue-400" />
                Financial Settings
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Initial Capital
                  </label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                    <input
                      type="number"
                      name="initialCapital"
                      value={formData.initialCapital}
                      onChange={handleInputChange}
                      className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="100000"
                      min="1000"
                      step="1000"
                      required
                      disabled={isEditing} // Can't change initial capital when editing
                    />
                  </div>
                  {isEditing && (
                    <p className="text-xs text-slate-500 mt-1">Initial capital cannot be changed after creation</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Currency
                  </label>
                  <select
                    name="currency"
                    value={formData.currency}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  >
                    <option value="USD">USD - US Dollar</option>
                    <option value="EUR">EUR - Euro</option>
                    <option value="GBP">GBP - British Pound</option>
                    <option value="JPY">JPY - Japanese Yen</option>
                    <option value="INR">INR - Indian Rupee</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Benchmark
                  </label>
                  <input
                    type="text"
                    name="benchmarkSymbol"
                    value={formData.benchmarkSymbol}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    placeholder="SPY"
                  />
                </div>
              </div>
            </div>

            {/* Management Settings */}
            {user?.role === UserRole.PORTFOLIO_MANAGER && (
              <div>
                <h3 className="text-lg font-medium text-white mb-4 flex items-center">
                  <Users className="w-5 h-5 mr-2 text-purple-400" />
                  Management Settings
                </h3>
                
                <div className="bg-blue-500/10 border border-blue-500/20 rounded-lg p-4">
                  <div className="flex items-center text-blue-400 text-sm mb-2">
                    <AlertCircle className="w-4 h-4 mr-1" />
                    Portfolio Manager Features
                  </div>
                  <p className="text-xs text-slate-400">
                    As a Portfolio Manager, you can create portfolios for clients and manage them on their behalf.
                    Client assignment and delegation features will be available in future updates.
                  </p>
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="flex justify-end space-x-4 pt-6 border-t border-slate-700">
              <Link
                to="/portfolios"
                className="px-6 py-3 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
              >
                Cancel
              </Link>
              <button
                type="submit"
                disabled={loading}
                className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 disabled:opacity-50 disabled:transform-none flex items-center"
              >
                <Save className="w-5 h-5 mr-2" />
                {loading ? 'Saving...' : (isEditing ? 'Update Portfolio' : 'Create Portfolio')}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};

export default PortfolioBuilderPage;