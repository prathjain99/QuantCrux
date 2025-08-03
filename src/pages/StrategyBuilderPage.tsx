import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { 
  Save, 
  Play, 
  TrendingUp, 
  ArrowLeft, 
  Settings, 
  Tag,
  Activity,
  AlertCircle,
  CheckCircle,
  XCircle
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { strategyService, StrategyRequest, StrategyStatus, SignalEvaluationRequest, SignalEvaluationResponse, SignalType } from '../services/strategyService';
import toast from 'react-hot-toast';

const StrategyBuilderPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = !!id;

  const [formData, setFormData] = useState<StrategyRequest>({
    name: '',
    description: '',
    symbol: 'AAPL',
    timeframe: '15m',
    configJson: JSON.stringify({
      indicators: [
        { type: "RSI", period: 14 },
        { type: "SMA", period: 50 }
      ],
      entry: {
        logic: "AND",
        rules: [
          { indicator: "RSI", operator: "<", value: 30 },
          { indicator: "Price", operator: ">", compare_to: "SMA_50" }
        ]
      },
      exit: {
        logic: "OR",
        rules: [
          { indicator: "RSI", operator: ">", value: 70 },
          { stop_loss: 5 }
        ]
      },
      position: {
        capital_pct: 25,
        leverage: 1
      }
    }, null, 2),
    status: StrategyStatus.DRAFT,
    tags: []
  });

  const [loading, setLoading] = useState(false);
  const [testing, setTesting] = useState(false);
  const [testResult, setTestResult] = useState<SignalEvaluationResponse | null>(null);
  const [tagInput, setTagInput] = useState('');

  useEffect(() => {
    if (isEditing) {
      loadStrategy();
    }
  }, [id]);

  const loadStrategy = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      const strategy = await strategyService.getStrategy(id);
      setFormData({
        name: strategy.name,
        description: strategy.description || '',
        symbol: strategy.symbol,
        timeframe: strategy.timeframe,
        configJson: strategy.configJson,
        status: strategy.status,
        tags: strategy.tags || []
      });
    } catch (error: any) {
      toast.error('Failed to load strategy');
      navigate('/strategies');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleConfigChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setFormData(prev => ({
      ...prev,
      configJson: e.target.value
    }));
  };

  const addTag = () => {
    if (tagInput.trim() && !formData.tags?.includes(tagInput.trim())) {
      setFormData(prev => ({
        ...prev,
        tags: [...(prev.tags || []), tagInput.trim()]
      }));
      setTagInput('');
    }
  };

  const removeTag = (tagToRemove: string) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags?.filter(tag => tag !== tagToRemove) || []
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      
      // Validate JSON
      JSON.parse(formData.configJson);
      
      if (isEditing && id) {
        await strategyService.updateStrategy(id, formData);
        toast.success('Strategy updated successfully');
      } else {
        await strategyService.createStrategy(formData);
        toast.success('Strategy created successfully');
      }
      
      navigate('/strategies');
    } catch (error: any) {
      if (error instanceof SyntaxError) {
        toast.error('Invalid JSON configuration');
      } else {
        toast.error(error.response?.data?.message || 'Failed to save strategy');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleTestStrategy = async () => {
    try {
      setTesting(true);
      
      // Validate JSON first
      JSON.parse(formData.configJson);
      
      const testRequest: SignalEvaluationRequest = {
        symbol: formData.symbol,
        configJson: formData.configJson,
        timeframe: formData.timeframe
      };
      
      const result = await strategyService.evaluateStrategy(testRequest);
      setTestResult(result);
      toast.success('Strategy tested successfully');
    } catch (error: any) {
      if (error instanceof SyntaxError) {
        toast.error('Invalid JSON configuration');
      } else {
        toast.error('Failed to test strategy');
      }
    } finally {
      setTesting(false);
    }
  };

  const getSignalColor = (signal: SignalType) => {
    switch (signal) {
      case SignalType.BUY:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      case SignalType.SELL:
        return 'text-red-400 bg-red-500/10 border-red-500/20';
      case SignalType.HOLD:
        return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
    }
  };

  const getSignalIcon = (signal: SignalType) => {
    switch (signal) {
      case SignalType.BUY:
        return <CheckCircle className="w-4 h-4" />;
      case SignalType.SELL:
        return <XCircle className="w-4 h-4" />;
      case SignalType.HOLD:
        return <AlertCircle className="w-4 h-4" />;
      default:
        return <AlertCircle className="w-4 h-4" />;
    }
  };

  if (loading && isEditing) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading strategy...</p>
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
                <Link to="/strategies" className="text-slate-400 hover:text-white transition-colors flex items-center">
                  <ArrowLeft className="w-4 h-4 mr-1" />
                  Strategies
                </Link>
                <span className="text-slate-600">/</span>
                <span className="text-emerald-400 font-medium">
                  {isEditing ? 'Edit Strategy' : 'New Strategy'}
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
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Strategy Form */}
          <div className="lg:col-span-2">
            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
              <div className="flex items-center mb-6">
                <Settings className="w-6 h-6 text-emerald-400 mr-3" />
                <h2 className="text-xl font-semibold text-white">Strategy Configuration</h2>
              </div>

              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Basic Info */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Strategy Name
                    </label>
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="Enter strategy name"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Symbol
                    </label>
                    <input
                      type="text"
                      name="symbol"
                      value={formData.symbol}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="e.g., AAPL, BTCUSD"
                      required
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Timeframe
                    </label>
                    <select
                      name="timeframe"
                      value={formData.timeframe}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value="1m">1 Minute</option>
                      <option value="5m">5 Minutes</option>
                      <option value="15m">15 Minutes</option>
                      <option value="30m">30 Minutes</option>
                      <option value="1h">1 Hour</option>
                      <option value="4h">4 Hours</option>
                      <option value="1d">1 Day</option>
                    </select>
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
                      <option value={StrategyStatus.DRAFT}>Draft</option>
                      <option value={StrategyStatus.ACTIVE}>Active</option>
                      <option value={StrategyStatus.PAUSED}>Paused</option>
                      <option value={StrategyStatus.ARCHIVED}>Archived</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Description
                  </label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleInputChange}
                    rows={3}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    placeholder="Describe your strategy..."
                  />
                </div>

                {/* Tags */}
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Tags
                  </label>
                  <div className="flex flex-wrap gap-2 mb-3">
                    {formData.tags?.map((tag, index) => (
                      <span
                        key={index}
                        className="inline-flex items-center px-3 py-1 rounded-full bg-emerald-500/20 text-emerald-400 text-sm"
                      >
                        <Tag className="w-3 h-3 mr-1" />
                        {tag}
                        <button
                          type="button"
                          onClick={() => removeTag(tag)}
                          className="ml-2 text-emerald-400 hover:text-emerald-300"
                        >
                          ×
                        </button>
                      </span>
                    ))}
                  </div>
                  <div className="flex">
                    <input
                      type="text"
                      value={tagInput}
                      onChange={(e) => setTagInput(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())}
                      className="flex-1 px-4 py-2 bg-slate-800/50 border border-slate-600 rounded-l-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="Add tag..."
                    />
                    <button
                      type="button"
                      onClick={addTag}
                      className="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 text-white rounded-r-lg transition-colors"
                    >
                      Add
                    </button>
                  </div>
                </div>

                {/* Strategy Configuration JSON */}
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Strategy Configuration (JSON)
                  </label>
                  <textarea
                    value={formData.configJson}
                    onChange={handleConfigChange}
                    rows={20}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200 font-mono text-sm"
                    placeholder="Enter strategy configuration as JSON..."
                    required
                  />
                </div>

                {/* Actions */}
                <div className="flex justify-between">
                  <button
                    type="button"
                    onClick={handleTestStrategy}
                    disabled={testing}
                    className="bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 font-semibold py-3 px-6 rounded-lg transition-all duration-200 flex items-center disabled:opacity-50"
                  >
                    <Play className="w-5 h-5 mr-2" />
                    {testing ? 'Testing...' : 'Test Strategy'}
                  </button>

                  <button
                    type="submit"
                    disabled={loading}
                    className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 disabled:opacity-50 disabled:transform-none flex items-center"
                  >
                    <Save className="w-5 h-5 mr-2" />
                    {loading ? 'Saving...' : (isEditing ? 'Update Strategy' : 'Create Strategy')}
                  </button>
                </div>
              </form>
            </div>
          </div>

          {/* Test Results */}
          <div className="lg:col-span-1">
            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
              <div className="flex items-center mb-6">
                <Activity className="w-6 h-6 text-blue-400 mr-3" />
                <h2 className="text-xl font-semibold text-white">Live Test Results</h2>
              </div>

              {testResult ? (
                <div className="space-y-4">
                  {/* Signal */}
                  <div className="text-center">
                    <div className={`inline-flex items-center px-4 py-2 rounded-full border text-lg font-semibold ${getSignalColor(testResult.signal)}`}>
                      {getSignalIcon(testResult.signal)}
                      <span className="ml-2">{testResult.signal}</span>
                    </div>
                  </div>

                  {/* Current Price */}
                  <div className="bg-slate-800/50 rounded-lg p-4">
                    <div className="text-sm text-slate-400 mb-1">Current Price</div>
                    <div className="text-2xl font-bold text-white">
                      ${testResult.currentPrice?.toFixed(2)}
                    </div>
                  </div>

                  {/* Indicators */}
                  {testResult.indicatorValues && (
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <div className="text-sm text-slate-400 mb-3">Indicators</div>
                      <div className="space-y-2">
                        {Object.entries(testResult.indicatorValues).map(([key, value]) => (
                          <div key={key} className="flex justify-between">
                            <span className="text-slate-300">{key}</span>
                            <span className="text-white font-medium">
                              {typeof value === 'number' ? value.toFixed(2) : value}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Matched Rules */}
                  {testResult.matchedRules && testResult.matchedRules.length > 0 && (
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <div className="text-sm text-slate-400 mb-3">Matched Rules</div>
                      <div className="space-y-1">
                        {testResult.matchedRules.map((rule, index) => (
                          <div key={index} className="text-sm text-emerald-400">
                            ✓ {rule}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Confidence Score */}
                  {testResult.confidenceScore && (
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <div className="text-sm text-slate-400 mb-1">Confidence Score</div>
                      <div className="text-lg font-semibold text-white">
                        {(testResult.confidenceScore * 100).toFixed(1)}%
                      </div>
                    </div>
                  )}

                  {/* Timestamp */}
                  <div className="text-xs text-slate-500 text-center">
                    Evaluated at {new Date(testResult.evaluatedAt).toLocaleTimeString()}
                  </div>
                </div>
              ) : (
                <div className="text-center py-8">
                  <Activity className="w-12 h-12 text-slate-600 mx-auto mb-4" />
                  <p className="text-slate-400 mb-4">No test results yet</p>
                  <p className="text-sm text-slate-500">
                    Click "Test Strategy" to see how your strategy performs with current market data
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default StrategyBuilderPage;