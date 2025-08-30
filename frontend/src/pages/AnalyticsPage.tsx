import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  TrendingUp, 
  BarChart3, 
  PieChart, 
  Activity,
  Download,
  Calendar,
  Filter,
  RefreshCw,
  AlertTriangle,
  CheckCircle,
  Clock,
  FileText,
  Target,
  TrendingDown,
  Shield
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart as RechartsPieChart, Pie, Cell } from 'recharts';
import { useAuth } from '../hooks/useAuth';
import { portfolioService, Portfolio } from '../services/portfolioService';
import { strategyService, Strategy } from '../services/strategyService';
import { 
  analyticsService, 
  AnalyticsData, 
  AnalyticsRequest, 
  Report, 
  ReportRequest, 
  ReportType, 
  FileFormat, 
  ReportStatus 
} from '../services/analyticsService';
import toast from 'react-hot-toast';

const AnalyticsPage: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'risk' | 'performance' | 'attribution' | 'reports'>('risk');
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [strategies, setStrategies] = useState<Strategy[]>([]);
  const [reports, setReports] = useState<Report[]>([]);
  const [selectedPortfolio, setSelectedPortfolio] = useState<string>('');
  const [selectedStrategy, setSelectedStrategy] = useState<string>('');
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(false);
  const [showReportForm, setShowReportForm] = useState(false);

  // Date range state
  const [dateRange, setDateRange] = useState({
    start: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 30 days ago
    end: new Date().toISOString().split('T')[0] // Today
  });

  // Report form state
  const [reportForm, setReportForm] = useState<ReportRequest>({
    reportType: ReportType.PORTFOLIO_SUMMARY,
    reportName: '',
    fileFormat: FileFormat.PDF,
    benchmarkSymbol: 'SPY',
    includeCharts: true,
    includeCorrelations: false,
    includeAttribution: false,
    includeBenchmarkComparison: true
  });

  useEffect(() => {
    loadPortfolios();
    loadStrategies();
    loadReports();
  }, []);

  useEffect(() => {
    if (selectedPortfolio && activeTab !== 'reports') {
      loadPortfolioAnalytics();
    }
  }, [selectedPortfolio, dateRange, activeTab]);

  useEffect(() => {
    if (selectedStrategy && activeTab !== 'reports') {
      loadStrategyAnalytics();
    }
  }, [selectedStrategy, dateRange, activeTab]);

  const loadPortfolios = async () => {
    try {
      const data = await portfolioService.getPortfolios();
      setPortfolios(data);
      if (data.length > 0 && !selectedPortfolio) {
        setSelectedPortfolio(data[0].id);
      }
    } catch (error: any) {
      console.error('Failed to load portfolios:', error);
    }
  };

  const loadStrategies = async () => {
    try {
      const data = await strategyService.getStrategies();
      setStrategies(data);
    } catch (error: any) {
      console.error('Failed to load strategies:', error);
    }
  };

  const loadReports = async () => {
    try {
      const data = await analyticsService.getReports();
      setReports(data);
    } catch (error: any) {
      console.error('Failed to load reports:', error);
    }
  };

  const loadPortfolioAnalytics = async () => {
    if (!selectedPortfolio) return;
    
    try {
      setLoading(true);
      const request: AnalyticsRequest = {
        portfolioId: selectedPortfolio,
        periodStart: dateRange.start,
        periodEnd: dateRange.end,
        benchmarkSymbol: 'SPY',
        includeCorrelations: activeTab === 'attribution',
        includeAttribution: activeTab === 'attribution'
      };
      
      const data = await analyticsService.getPortfolioAnalytics(selectedPortfolio, request);
      setAnalyticsData(data);
    } catch (error: any) {
      toast.error('Failed to load analytics data');
      console.error('Error loading analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadStrategyAnalytics = async () => {
    if (!selectedStrategy) return;
    
    try {
      setLoading(true);
      const request: AnalyticsRequest = {
        strategyId: selectedStrategy,
        periodStart: dateRange.start,
        periodEnd: dateRange.end,
        benchmarkSymbol: 'SPY'
      };
      
      const data = await analyticsService.getStrategyAnalytics(selectedStrategy, request);
      setAnalyticsData(data);
    } catch (error: any) {
      toast.error('Failed to load analytics data');
      console.error('Error loading analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateReport = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const request: ReportRequest = {
        ...reportForm,
        portfolioId: selectedPortfolio || undefined,
        strategyId: selectedStrategy || undefined,
        periodStart: dateRange.start,
        periodEnd: dateRange.end
      };
      
      await analyticsService.generateReport(request);
      toast.success('Report generation started');
      setShowReportForm(false);
      loadReports();
    } catch (error: any) {
      toast.error('Failed to generate report');
    }
  };

  const handleDownloadReport = async (reportId: string) => {
    try {
      const blob = await analyticsService.downloadReport(reportId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report_${reportId}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error: any) {
      toast.error('Failed to download report');
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

  const getReportStatusColor = (status: ReportStatus) => {
    switch (status) {
      case ReportStatus.COMPLETED:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      case ReportStatus.GENERATING:
        return 'text-blue-400 bg-blue-500/10 border-blue-500/20';
      case ReportStatus.PENDING:
        return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
      case ReportStatus.FAILED:
        return 'text-red-400 bg-red-500/10 border-red-500/20';
      case ReportStatus.EXPIRED:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
    }
  };

  const getReportStatusIcon = (status: ReportStatus) => {
    switch (status) {
      case ReportStatus.COMPLETED:
        return <CheckCircle className="w-3 h-3" />;
      case ReportStatus.GENERATING:
        return <RefreshCw className="w-3 h-3 animate-spin" />;
      case ReportStatus.PENDING:
        return <Clock className="w-3 h-3" />;
      case ReportStatus.FAILED:
        return <AlertTriangle className="w-3 h-3" />;
      case ReportStatus.EXPIRED:
        return <AlertTriangle className="w-3 h-3" />;
      default:
        return <Clock className="w-3 h-3" />;
    }
  };

  // Prepare chart data
  const pieColors = ['#10B981', '#3B82F6', '#8B5CF6', '#F59E0B', '#EF4444', '#6B7280'];

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
                <span className="text-emerald-400 font-medium">Analytics</span>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <button
                onClick={() => setShowReportForm(true)}
                className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-2 px-4 rounded-lg transition-all duration-200 flex items-center"
              >
                <Download className="w-4 h-4 mr-2" />
                Generate Report
              </button>
              <span className="text-sm text-slate-400">{user?.fullName}</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h2 className="text-3xl font-bold text-white mb-2">Analytics & Reporting</h2>
            <p className="text-slate-400">Comprehensive risk analysis and performance insights</p>
          </div>
        </div>

        {/* Controls */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 mb-8">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Portfolio
              </label>
              <select
                value={selectedPortfolio}
                onChange={(e) => setSelectedPortfolio(e.target.value)}
                className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
              >
                <option value="">Select Portfolio</option>
                {portfolios.map((portfolio) => (
                  <option key={portfolio.id} value={portfolio.id}>
                    {portfolio.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Start Date
              </label>
              <input
                type="date"
                value={dateRange.start}
                onChange={(e) => setDateRange(prev => ({ ...prev, start: e.target.value }))}
                className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                End Date
              </label>
              <input
                type="date"
                value={dateRange.end}
                onChange={(e) => setDateRange(prev => ({ ...prev, end: e.target.value }))}
                className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
              />
            </div>

            <div className="flex items-end">
              <button
                onClick={selectedPortfolio ? loadPortfolioAnalytics : loadStrategyAnalytics}
                disabled={loading || (!selectedPortfolio && !selectedStrategy)}
                className="w-full bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 font-semibold py-3 px-4 rounded-lg transition-all duration-200 flex items-center justify-center disabled:opacity-50"
              >
                <RefreshCw className={`w-4 h-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
                {loading ? 'Loading...' : 'Refresh'}
              </button>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 mb-8">
          <div className="border-b border-slate-700">
            <nav className="flex space-x-8 px-6">
              {[
                { id: 'risk', label: 'Risk Analytics', icon: Shield },
                { id: 'performance', label: 'Performance', icon: TrendingUp },
                { id: 'attribution', label: 'Attribution', icon: PieChart },
                { id: 'reports', label: 'Reports', icon: FileText }
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
            {/* Risk Analytics Tab */}
            {activeTab === 'risk' && (
              <div className="space-y-8">
                {analyticsData ? (
                  <>
                    {/* Risk Metrics Grid */}
                    <div>
                      <h3 className="text-lg font-semibold text-white mb-4">Risk Metrics</h3>
                      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="flex items-center text-slate-400 text-sm mb-2">
                            <TrendingDown className="w-4 h-4 mr-1" />
                            VaR (95%)
                          </div>
                          <div className="text-xl font-bold text-red-400">
                            {formatCurrency(analyticsData.var95)}
                          </div>
                        </div>

                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">Volatility</div>
                          <div className="text-xl font-bold text-purple-400">
                            {formatPercentage(analyticsData.volatility)}
                          </div>
                        </div>

                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">Beta</div>
                          <div className="text-xl font-bold text-blue-400">
                            {formatMetric(analyticsData.beta)}
                          </div>
                        </div>

                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">Sharpe Ratio</div>
                          <div className="text-xl font-bold text-emerald-400">
                            {formatMetric(analyticsData.sharpeRatio)}
                          </div>
                        </div>

                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">Max Drawdown</div>
                          <div className="text-xl font-bold text-red-400">
                            {formatPercentage(analyticsData.maxDrawdown)}
                          </div>
                        </div>

                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">Alpha</div>
                          <div className={`text-xl font-bold ${getReturnColor(analyticsData.alpha)}`}>
                            {formatPercentage(analyticsData.alpha)}
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Benchmark Comparison */}
                    <div>
                      <h3 className="text-lg font-semibold text-white mb-4">Benchmark Comparison</h3>
                      <div className="bg-slate-800/50 rounded-lg p-6">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                          <div className="text-center">
                            <div className="text-slate-400 text-sm mb-1">Portfolio Return</div>
                            <div className={`text-2xl font-bold ${getReturnColor(analyticsData.totalReturn)}`}>
                              {formatPercentage(analyticsData.totalReturn)}
                            </div>
                          </div>
                          <div className="text-center">
                            <div className="text-slate-400 text-sm mb-1">Benchmark Return ({analyticsData.benchmarkSymbol})</div>
                            <div className={`text-2xl font-bold ${getReturnColor(analyticsData.benchmarkReturn)}`}>
                              {formatPercentage(analyticsData.benchmarkReturn)}
                            </div>
                          </div>
                          <div className="text-center">
                            <div className="text-slate-400 text-sm mb-1">Outperformance</div>
                            <div className={`text-2xl font-bold ${getReturnColor(analyticsData.outperformance)}`}>
                              {formatPercentage(analyticsData.outperformance)}
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </>
                ) : (
                  <div className="text-center py-12">
                    <BarChart3 className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-white mb-2">No Analytics Data</h3>
                    <p className="text-slate-400">Select a portfolio and date range to view risk analytics</p>
                  </div>
                )}
              </div>
            )}

            {/* Performance Tab */}
            {activeTab === 'performance' && (
              <div className="space-y-8">
                {analyticsData ? (
                  <>
                    {/* Performance Metrics */}
                    <div>
                      <h3 className="text-lg font-semibold text-white mb-4">Performance Metrics</h3>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">Total Return</div>
                          <div className={`text-xl font-bold ${getReturnColor(analyticsData.totalReturn)}`}>
                            {formatPercentage(analyticsData.totalReturn)}
                          </div>
                        </div>

                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">CAGR</div>
                          <div className={`text-xl font-bold ${getReturnColor(analyticsData.cagr)}`}>
                            {formatPercentage(analyticsData.cagr)}
                          </div>
                        </div>

                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">Win Rate</div>
                          <div className="text-xl font-bold text-emerald-400">
                            {formatPercentage(analyticsData.winRate)}
                          </div>
                        </div>

                        <div className="bg-slate-800/50 rounded-xl border border-slate-700 p-4">
                          <div className="text-slate-400 text-sm mb-2">Total Trades</div>
                          <div className="text-xl font-bold text-white">
                            {analyticsData.totalTrades || 0}
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* Trade Analysis */}
                    <div>
                      <h3 className="text-lg font-semibold text-white mb-4">Trade Analysis</h3>
                      <div className="bg-slate-800/50 rounded-lg p-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                          <div>
                            <h4 className="text-white font-medium mb-3">Trade Statistics</h4>
                            <div className="space-y-2 text-sm">
                              <div className="flex justify-between">
                                <span className="text-slate-400">Total Trades</span>
                                <span className="text-white">{analyticsData.totalTrades || 0}</span>
                              </div>
                              <div className="flex justify-between">
                                <span className="text-slate-400">Winning Trades</span>
                                <span className="text-emerald-400">{analyticsData.winningTrades || 0}</span>
                              </div>
                              <div className="flex justify-between">
                                <span className="text-slate-400">Losing Trades</span>
                                <span className="text-red-400">{analyticsData.losingTrades || 0}</span>
                              </div>
                              <div className="flex justify-between">
                                <span className="text-slate-400">Profit Factor</span>
                                <span className="text-white">{formatMetric(analyticsData.profitFactor)}</span>
                              </div>
                            </div>
                          </div>

                          <div>
                            <h4 className="text-white font-medium mb-3">Efficiency Metrics</h4>
                            <div className="space-y-2 text-sm">
                              <div className="flex justify-between">
                                <span className="text-slate-400">Trade Frequency</span>
                                <span className="text-white">{formatMetric(analyticsData.tradeFrequency)} /month</span>
                              </div>
                              <div className="flex justify-between">
                                <span className="text-slate-400">Avg Holding Period</span>
                                <span className="text-white">{analyticsData.avgHoldingPeriod || 'N/A'} days</span>
                              </div>
                              <div className="flex justify-between">
                                <span className="text-slate-400">Turnover Ratio</span>
                                <span className="text-white">{formatPercentage(analyticsData.turnoverRatio)}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </>
                ) : (
                  <div className="text-center py-12">
                    <Activity className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-white mb-2">No Performance Data</h3>
                    <p className="text-slate-400">Select a portfolio and date range to view performance metrics</p>
                  </div>
                )}
              </div>
            )}

            {/* Attribution Tab */}
            {activeTab === 'attribution' && (
              <div className="space-y-8">
                {analyticsData && (analyticsData.assetAttribution || analyticsData.sectorAttribution) ? (
                  <>
                    {/* Attribution Charts */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                      {/* Asset Attribution */}
                      {analyticsData.assetAttribution && (
                        <div>
                          <h3 className="text-lg font-semibold text-white mb-4">Asset Attribution</h3>
                          <div className="bg-slate-800/50 rounded-lg p-4">
                            <ResponsiveContainer width="100%" height={250}>
                              <RechartsPieChart>
                                <Pie
                                  data={Object.entries(analyticsData.assetAttribution).map(([key, value]) => ({
                                    name: key,
                                    value: value * 100
                                  }))}
                                  cx="50%"
                                  cy="50%"
                                  outerRadius={80}
                                  dataKey="value"
                                  nameKey="name"
                                >
                                  {Object.keys(analyticsData.assetAttribution).map((_, index) => (
                                    <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                                  ))}
                                </Pie>
                                <Tooltip 
                                  formatter={(value: any) => [`${value.toFixed(1)}%`, 'Contribution']}
                                  contentStyle={{
                                    backgroundColor: '#1F2937',
                                    border: '1px solid #374151',
                                    borderRadius: '8px',
                                    color: '#F9FAFB'
                                  }}
                                />
                              </RechartsPieChart>
                            </ResponsiveContainer>
                          </div>
                        </div>
                      )}

                      {/* Sector Attribution */}
                      {analyticsData.sectorAttribution && (
                        <div>
                          <h3 className="text-lg font-semibold text-white mb-4">Sector Attribution</h3>
                          <div className="bg-slate-800/50 rounded-lg p-4">
                            <ResponsiveContainer width="100%" height={250}>
                              <RechartsPieChart>
                                <Pie
                                  data={Object.entries(analyticsData.sectorAttribution).map(([key, value]) => ({
                                    name: key,
                                    value: value * 100
                                  }))}
                                  cx="50%"
                                  cy="50%"
                                  outerRadius={80}
                                  dataKey="value"
                                  nameKey="name"
                                >
                                  {Object.keys(analyticsData.sectorAttribution).map((_, index) => (
                                    <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                                  ))}
                                </Pie>
                                <Tooltip 
                                  formatter={(value: any) => [`${value.toFixed(1)}%`, 'Contribution']}
                                  contentStyle={{
                                    backgroundColor: '#1F2937',
                                    border: '1px solid #374151',
                                    borderRadius: '8px',
                                    color: '#F9FAFB'
                                  }}
                                />
                              </RechartsPieChart>
                            </ResponsiveContainer>
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Correlation Matrix */}
                    {analyticsData.correlationMatrix && (
                      <div>
                        <h3 className="text-lg font-semibold text-white mb-4">Correlation Analysis</h3>
                        <div className="bg-slate-800/50 rounded-lg p-6">
                          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                            <div className="text-center">
                              <div className="text-slate-400 text-sm mb-1">Average Correlation</div>
                              <div className="text-xl font-bold text-white">
                                {formatMetric(analyticsData.avgCorrelation)}
                              </div>
                            </div>
                            <div className="text-center">
                              <div className="text-slate-400 text-sm mb-1">Max Correlation</div>
                              <div className="text-xl font-bold text-red-400">
                                {formatMetric(analyticsData.maxCorrelation)}
                              </div>
                            </div>
                            <div className="text-center">
                              <div className="text-slate-400 text-sm mb-1">Diversification Ratio</div>
                              <div className="text-xl font-bold text-emerald-400">
                                {formatMetric(analyticsData.diversificationRatio)}
                              </div>
                            </div>
                          </div>
                          
                          <div className="text-sm text-slate-400">
                            <p>Correlation matrix visualization would be implemented here using a heatmap component.</p>
                          </div>
                        </div>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="text-center py-12">
                    <PieChart className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-white mb-2">No Attribution Data</h3>
                    <p className="text-slate-400">Select a portfolio and enable attribution analysis to view contribution breakdown</p>
                  </div>
                )}
              </div>
            )}

            {/* Reports Tab */}
            {activeTab === 'reports' && (
              <div className="space-y-6">
                {/* Reports List */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Generated Reports</h3>
                  {reports.length === 0 ? (
                    <div className="bg-slate-800/50 rounded-lg p-8 text-center">
                      <FileText className="w-12 h-12 text-slate-600 mx-auto mb-4" />
                      <p className="text-slate-400">No reports generated yet</p>
                    </div>
                  ) : (
                    <div className="bg-slate-800/50 rounded-lg overflow-hidden">
                      <div className="overflow-x-auto">
                        <table className="w-full">
                          <thead className="bg-slate-700/50">
                            <tr>
                              <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Report</th>
                              <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Type</th>
                              <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Period</th>
                              <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Status</th>
                              <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Created</th>
                              <th className="px-4 py-3 text-center text-xs font-medium text-slate-300 uppercase">Actions</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-700">
                            {reports.map((report) => (
                              <tr key={report.id} className="hover:bg-slate-700/30">
                                <td className="px-4 py-3">
                                  <div className="text-sm font-medium text-white">{report.reportName}</div>
                                  <div className="text-xs text-slate-400">
                                    {report.portfolioName || report.strategyName || 'N/A'}
                                  </div>
                                </td>
                                <td className="px-4 py-3">
                                  <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-500/20 text-blue-400">
                                    {report.reportType.replace('_', ' ')}
                                  </span>
                                </td>
                                <td className="px-4 py-3 text-sm text-slate-300">
                                  {report.periodStart && report.periodEnd ? 
                                    `${new Date(report.periodStart).toLocaleDateString()} - ${new Date(report.periodEnd).toLocaleDateString()}` :
                                    'N/A'
                                  }
                                </td>
                                <td className="px-4 py-3">
                                  <div className={`inline-flex items-center px-2 py-1 rounded-full border text-xs font-medium ${getReportStatusColor(report.status)}`}>
                                    {getReportStatusIcon(report.status)}
                                    <span className="ml-1">{report.status}</span>
                                  </div>
                                </td>
                                <td className="px-4 py-3 text-sm text-slate-300">
                                  {new Date(report.createdAt).toLocaleDateString()}
                                </td>
                                <td className="px-4 py-3 text-center">
                                  {report.status === ReportStatus.COMPLETED && (
                                    <button
                                      onClick={() => handleDownloadReport(report.id)}
                                      className="text-emerald-400 hover:text-emerald-300 transition-colors"
                                      title="Download Report"
                                    >
                                      <Download className="w-4 h-4" />
                                    </button>
                                  )}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Report Generation Modal */}
        {showReportForm && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-slate-900 rounded-xl border border-slate-700 w-full max-w-2xl">
              <div className="flex items-center justify-between p-6 border-b border-slate-700">
                <h2 className="text-xl font-semibold text-white">Generate Report</h2>
                <button
                  onClick={() => setShowReportForm(false)}
                  className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleGenerateReport} className="p-6 space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Report Name
                    </label>
                    <input
                      type="text"
                      value={reportForm.reportName}
                      onChange={(e) => setReportForm(prev => ({ ...prev, reportName: e.target.value }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="Monthly Portfolio Report"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Report Type
                    </label>
                    <select
                      value={reportForm.reportType}
                      onChange={(e) => setReportForm(prev => ({ ...prev, reportType: e.target.value as ReportType }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value={ReportType.PORTFOLIO_SUMMARY}>Portfolio Summary</option>
                      <option value={ReportType.RISK_ANALYSIS}>Risk Analysis</option>
                      <option value={ReportType.PERFORMANCE_REPORT}>Performance Report</option>
                      <option value={ReportType.ATTRIBUTION_ANALYSIS}>Attribution Analysis</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Portfolio
                    </label>
                    <select
                      value={reportForm.portfolioId || ''}
                      onChange={(e) => setReportForm(prev => ({ ...prev, portfolioId: e.target.value || undefined }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value="">Select Portfolio</option>
                      {portfolios.map((portfolio) => (
                        <option key={portfolio.id} value={portfolio.id}>
                          {portfolio.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      File Format
                    </label>
                    <select
                      value={reportForm.fileFormat}
                      onChange={(e) => setReportForm(prev => ({ ...prev, fileFormat: e.target.value as FileFormat }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value={FileFormat.PDF}>PDF</option>
                      <option value={FileFormat.CSV}>CSV</option>
                      <option value={FileFormat.XLS}>Excel</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Description (Optional)
                  </label>
                  <textarea
                    value={reportForm.description || ''}
                    onChange={(e) => setReportForm(prev => ({ ...prev, description: e.target.value }))}
                    rows={3}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    placeholder="Report description..."
                  />
                </div>

                <div className="flex justify-end space-x-4 pt-4 border-t border-slate-700">
                  <button
                    type="button"
                    onClick={() => setShowReportForm(false)}
                    className="px-6 py-3 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105"
                  >
                    Generate Report
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default AnalyticsPage;