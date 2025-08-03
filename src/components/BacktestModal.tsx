import React, { useState } from 'react';
import { X, Calendar, DollarSign, Settings, Play } from 'lucide-react';
import { Strategy } from '../services/strategyService';
import { backtestService, BacktestRequest } from '../services/backtestService';
import toast from 'react-hot-toast';

interface BacktestModalProps {
  isOpen: boolean;
  onClose: () => void;
  strategy: Strategy;
  onBacktestCreated: (backtestId: string) => void;
}

const BacktestModal: React.FC<BacktestModalProps> = ({
  isOpen,
  onClose,
  strategy,
  onBacktestCreated
}) => {
  const [formData, setFormData] = useState<BacktestRequest>({
    strategyId: strategy.id,
    name: `${strategy.name} Backtest`,
    symbol: strategy.symbol,
    timeframe: strategy.timeframe,
    startDate: '2023-01-01',
    endDate: '2023-12-31',
    initialCapital: 100000,
    commissionRate: 0.001,
    slippageRate: 0.0005
  });
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'initialCapital' || name === 'commissionRate' || name === 'slippageRate' 
        ? parseFloat(value) || 0 
        : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      const backtest = await backtestService.createBacktest(formData);
      toast.success('Backtest started successfully!');
      onBacktestCreated(backtest.id);
      onClose();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to start backtest');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-slate-900 rounded-xl border border-slate-700 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-slate-700">
          <div>
            <h2 className="text-xl font-semibold text-white">Run Backtest</h2>
            <p className="text-sm text-slate-400 mt-1">
              Test "{strategy.name}" with historical data
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Basic Settings */}
          <div>
            <h3 className="text-lg font-medium text-white mb-4 flex items-center">
              <Settings className="w-5 h-5 mr-2 text-emerald-400" />
              Basic Settings
            </h3>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Backtest Name
                </label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  placeholder="Enter backtest name"
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
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Date Range */}
          <div>
            <h3 className="text-lg font-medium text-white mb-4 flex items-center">
              <Calendar className="w-5 h-5 mr-2 text-blue-400" />
              Date Range
            </h3>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Start Date
                </label>
                <input
                  type="date"
                  name="startDate"
                  value={formData.startDate}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  End Date
                </label>
                <input
                  type="date"
                  name="endDate"
                  value={formData.endDate}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  required
                />
              </div>
            </div>
          </div>

          {/* Advanced Settings */}
          <div>
            <h3 className="text-lg font-medium text-white mb-4">Advanced Settings</h3>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Commission Rate (%)
                </label>
                <input
                  type="number"
                  name="commissionRate"
                  value={formData.commissionRate}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  placeholder="0.001"
                  min="0"
                  max="1"
                  step="0.0001"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Slippage Rate (%)
                </label>
                <input
                  type="number"
                  name="slippageRate"
                  value={formData.slippageRate}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  placeholder="0.0005"
                  min="0"
                  max="1"
                  step="0.0001"
                />
              </div>
            </div>
          </div>

          {/* Strategy Preview */}
          <div className="bg-slate-800/30 rounded-lg p-4">
            <h4 className="text-sm font-medium text-slate-300 mb-2">Strategy Configuration</h4>
            <div className="text-xs text-slate-400 space-y-1">
              <div>Strategy: {strategy.name}</div>
              <div>Version: {strategy.currentVersion}</div>
              <div>Status: {strategy.status}</div>
              <div>Tags: {strategy.tags?.join(', ') || 'None'}</div>
            </div>
          </div>

          {/* Actions */}
          <div className="flex justify-end space-x-4 pt-4 border-t border-slate-700">
            <button
              type="button"
              onClick={onClose}
              className="px-6 py-3 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 disabled:opacity-50 disabled:transform-none flex items-center"
            >
              <Play className="w-5 h-5 mr-2" />
              {loading ? 'Starting Backtest...' : 'Run Backtest'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default BacktestModal;