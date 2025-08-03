import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  TrendingUp, 
  Search, 
  Filter,
  RefreshCw,
  BarChart3,
  Activity,
  Globe,
  Star,
  Clock,
  AlertTriangle,
  CheckCircle,
  TrendingDown
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useAuth } from '../hooks/useAuth';
import { 
  marketDataService, 
  MarketData, 
  SymbolSearch, 
  AssetType, 
  DataType,
  OHLCVData
} from '../services/marketDataService';
import toast from 'react-hot-toast';

const MarketDataPage: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'live' | 'search' | 'charts' | 'watchlist'>('live');
  const [popularSymbols, setPopularSymbols] = useState<SymbolSearch[]>([]);
  const [liveData, setLiveData] = useState<Map<string, MarketData>>(new Map());
  const [searchResults, setSearchResults] = useState<SymbolSearch[]>([]);
  const [chartData, setChartData] = useState<OHLCVData[]>([]);
  const [selectedSymbol, setSelectedSymbol] = useState<string>('AAPL');
  const [searchQuery, setSearchQuery] = useState('');
  const [assetTypeFilter, setAssetTypeFilter] = useState<AssetType | 'ALL'>('ALL');
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    loadPopularSymbols();
    loadLiveData();
    
    // Auto-refresh every 30 seconds
    const interval = setInterval(() => {
      loadLiveData();
    }, 30000);
    
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (searchQuery.length >= 2) {
      searchSymbols();
    } else {
      setSearchResults([]);
    }
  }, [searchQuery]);

  useEffect(() => {
    if (selectedSymbol && activeTab === 'charts') {
      loadChartData();
    }
  }, [selectedSymbol, activeTab]);

  const loadPopularSymbols = async () => {
    try {
      const data = await marketDataService.getPopularSymbols(assetTypeFilter !== 'ALL' ? assetTypeFilter : undefined);
      setPopularSymbols(data);
    } catch (error: any) {
      console.error('Failed to load popular symbols:', error);
    }
  };

  const loadLiveData = async () => {
    try {
      const symbols = ['AAPL', 'MSFT', 'GOOGL', 'TSLA', 'AMZN', 'NVDA', 'META', 'NFLX', 'BTCUSD', 'ETHUSD', 'SPY', 'QQQ'];
      const dataMap = new Map<string, MarketData>();
      
      for (const symbol of symbols) {
        try {
          const data = await marketDataService.getLivePrice(symbol);
          dataMap.set(symbol, data);
        } catch (error) {
          console.error(`Failed to load data for ${symbol}:`, error);
        }
      }
      
      setLiveData(dataMap);
    } catch (error: any) {
      console.error('Failed to load live data:', error);
    }
  };

  const searchSymbols = async () => {
    try {
      setLoading(true);
      const results = await marketDataService.searchSymbols(searchQuery);
      setSearchResults(results);
    } catch (error: any) {
      console.error('Failed to search symbols:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadChartData = async () => {
    try {
      setLoading(true);
      const endTime = new Date().toISOString();
      const startTime = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(); // 30 days ago
      
      const data = await marketDataService.getOHLCVData(selectedSymbol, '1d', startTime, endTime, 30);
      setChartData(data.ohlcvData || []);
    } catch (error: any) {
      toast.error('Failed to load chart data');
      console.error('Failed to load chart data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRefreshCache = async () => {
    try {
      setRefreshing(true);
      await marketDataService.refreshCache();
      await loadLiveData();
      toast.success('Market data refreshed successfully');
    } catch (error: any) {
      toast.error('Failed to refresh market data');
    } finally {
      setRefreshing(false);
    }
  };

  const handleSymbolSelect = (symbol: string) => {
    setSelectedSymbol(symbol);
    if (activeTab === 'charts') {
      loadChartData();
    }
  };

  const filteredPopularSymbols = popularSymbols.filter(symbol => 
    assetTypeFilter === 'ALL' || symbol.assetType === assetTypeFilter
  );

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
                <span className="text-emerald-400 font-medium">Market Data</span>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <button
                onClick={handleRefreshCache}
                disabled={refreshing}
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                title="Refresh Market Data"
              >
                <RefreshCw className={`w-5 h-5 ${refreshing ? 'animate-spin' : ''}`} />
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
            <h2 className="text-3xl font-bold text-white mb-2">Market Data Service</h2>
            <p className="text-slate-400">Real-time prices, historical data, and symbol search</p>
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="text-right">
              <div className="text-sm text-slate-400">Data Sources</div>
              <div className="text-lg font-semibold text-emerald-400">Live</div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 mb-8">
          <div className="border-b border-slate-700">
            <nav className="flex space-x-8 px-6">
              {[
                { id: 'live', label: 'Live Prices', icon: Activity },
                { id: 'search', label: 'Symbol Search', icon: Search },
                { id: 'charts', label: 'Price Charts', icon: BarChart3 },
                { id: 'watchlist', label: 'Watchlist', icon: Star }
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
            {/* Live Prices Tab */}
            {activeTab === 'live' && (
              <div className="space-y-6">
                {/* Asset Type Filter */}
                <div className="flex items-center space-x-4">
                  <Filter className="w-5 h-5 text-slate-400" />
                  <select
                    value={assetTypeFilter}
                    onChange={(e) => setAssetTypeFilter(e.target.value as AssetType | 'ALL')}
                    className="px-4 py-2 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  >
                    <option value="ALL">All Asset Types</option>
                    <option value={AssetType.STOCK}>Stocks</option>
                    <option value={AssetType.CRYPTO}>Cryptocurrency</option>
                    <option value={AssetType.ETF}>ETFs</option>
                    <option value={AssetType.INDEX}>Indices</option>
                  </select>
                </div>

                {/* Live Price Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                  {Array.from(liveData.entries()).map(([symbol, data]) => (
                    <div key={symbol} className="bg-slate-800/50 rounded-xl border border-slate-700 p-4 hover:border-emerald-500/50 transition-all duration-300">
                      <div className="flex justify-between items-start mb-3">
                        <div>
                          <h3 className="text-lg font-semibold text-white">{symbol}</h3>
                          <div className="flex items-center space-x-2">
                            <span className="text-xs text-slate-400">{data.source}</span>
                            {data.isStale ? (
                              <AlertTriangle className="w-3 h-3 text-amber-400" title="Stale data" />
                            ) : (
                              <CheckCircle className="w-3 h-3 text-emerald-400" title="Fresh data" />
                            )}
                          </div>
                        </div>
                        <div className={`text-xs px-2 py-1 rounded ${
                          (data.dayChangePercent || 0) >= 0 ? 'bg-emerald-500/20 text-emerald-400' : 'bg-red-500/20 text-red-400'
                        }`}>
                          {marketDataService.formatPercentage(data.dayChangePercent)}
                        </div>
                      </div>

                      <div className="space-y-2">
                        <div className="text-2xl font-bold text-white">
                          {marketDataService.formatPrice(data.price)}
                        </div>
                        
                        <div className={`text-sm ${marketDataService.getTrendColor(data.dayChangePercent)}`}>
                          {marketDataService.formatPrice(data.dayChange)} ({marketDataService.formatPercentage(data.dayChangePercent)})
                        </div>

                        <div className="grid grid-cols-2 gap-2 text-xs">
                          <div className="bg-slate-700/50 rounded p-2">
                            <div className="text-slate-400">Bid</div>
                            <div className="text-white font-medium">{marketDataService.formatPrice(data.bidPrice)}</div>
                          </div>
                          <div className="bg-slate-700/50 rounded p-2">
                            <div className="text-slate-400">Ask</div>
                            <div className="text-white font-medium">{marketDataService.formatPrice(data.askPrice)}</div>
                          </div>
                        </div>

                        <div className="text-xs text-slate-400 flex items-center justify-between">
                          <span>Vol: {marketDataService.formatVolume(data.volume)}</span>
                          <span className="flex items-center">
                            <Clock className="w-3 h-3 mr-1" />
                            {new Date(data.dataTimestamp).toLocaleTimeString()}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Symbol Search Tab */}
            {activeTab === 'search' && (
              <div className="space-y-6">
                {/* Search Input */}
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                  <input
                    type="text"
                    placeholder="Search symbols (e.g., AAPL, Bitcoin, S&P 500)..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  />
                </div>

                {/* Search Results */}
                {searchResults.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">Search Results</h3>
                    <div className="space-y-2">
                      {searchResults.map((symbol) => (
                        <div key={symbol.symbol} className="bg-slate-800/50 rounded-lg border border-slate-700 p-4 hover:border-emerald-500/50 transition-all duration-300 cursor-pointer"
                             onClick={() => handleSymbolSelect(symbol.symbol)}>
                          <div className="flex justify-between items-start">
                            <div className="flex-1">
                              <div className="flex items-center space-x-3 mb-2">
                                <h4 className="text-lg font-semibold text-white">{symbol.symbol}</h4>
                                <span className={`px-2 py-1 rounded-full text-xs font-medium ${marketDataService.getAssetTypeColor(symbol.assetType)}`}>
                                  {symbol.assetType}
                                </span>
                              </div>
                              <p className="text-slate-300 mb-1">{symbol.name}</p>
                              <div className="flex items-center space-x-4 text-sm text-slate-400">
                                {symbol.exchange && (
                                  <span className="flex items-center">
                                    <Globe className="w-3 h-3 mr-1" />
                                    {symbol.exchange}
                                  </span>
                                )}
                                {symbol.sector && <span>{symbol.sector}</span>}
                                {symbol.country && <span>{symbol.country}</span>}
                              </div>
                            </div>
                            <div className="text-right">
                              {symbol.marketCap && (
                                <div className="text-sm text-slate-300">
                                  ${(symbol.marketCap / 1000000000).toFixed(1)}B
                                </div>
                              )}
                              <div className="text-xs text-slate-400">{symbol.currency}</div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Popular Symbols */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Popular Symbols</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {filteredPopularSymbols.slice(0, 12).map((symbol) => (
                      <div key={symbol.symbol} className="bg-slate-800/50 rounded-lg border border-slate-700 p-4 hover:border-emerald-500/50 transition-all duration-300 cursor-pointer"
                           onClick={() => handleSymbolSelect(symbol.symbol)}>
                        <div className="flex justify-between items-start mb-2">
                          <h4 className="text-lg font-semibold text-white">{symbol.symbol}</h4>
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${marketDataService.getAssetTypeColor(symbol.assetType)}`}>
                            {symbol.assetType}
                          </span>
                        </div>
                        <p className="text-slate-300 text-sm mb-2">{symbol.name}</p>
                        <div className="flex items-center justify-between text-xs text-slate-400">
                          <span>{symbol.exchange}</span>
                          {symbol.marketCap && (
                            <span>${(symbol.marketCap / 1000000000).toFixed(1)}B</span>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* Price Charts Tab */}
            {activeTab === 'charts' && (
              <div className="space-y-6">
                {/* Symbol Selector */}
                <div className="flex items-center space-x-4">
                  <label className="text-sm font-medium text-slate-300">Symbol:</label>
                  <select
                    value={selectedSymbol}
                    onChange={(e) => setSelectedSymbol(e.target.value)}
                    className="px-4 py-2 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                  >
                    {Array.from(liveData.keys()).map((symbol) => (
                      <option key={symbol} value={symbol}>{symbol}</option>
                    ))}
                  </select>
                  <button
                    onClick={loadChartData}
                    disabled={loading}
                    className="bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-4 py-2 rounded-lg transition-all duration-200 flex items-center"
                  >
                    <RefreshCw className={`w-4 h-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
                    {loading ? 'Loading...' : 'Refresh'}
                  </button>
                </div>

                {/* Price Chart */}
                {chartData.length > 0 ? (
                  <div className="bg-slate-800/50 rounded-lg p-6">
                    <h3 className="text-lg font-semibold text-white mb-4">{selectedSymbol} - 30 Day Price Chart</h3>
                    <ResponsiveContainer width="100%" height={400}>
                      <LineChart data={chartData}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                        <XAxis 
                          dataKey="timestamp" 
                          stroke="#9CA3AF"
                          fontSize={12}
                          tickFormatter={(value) => new Date(value).toLocaleDateString()}
                        />
                        <YAxis 
                          stroke="#9CA3AF"
                          fontSize={12}
                          tickFormatter={(value) => `$${value.toFixed(2)}`}
                        />
                        <Tooltip 
                          contentStyle={{
                            backgroundColor: '#1F2937',
                            border: '1px solid #374151',
                            borderRadius: '8px',
                            color: '#F9FAFB'
                          }}
                          formatter={(value: any) => [`$${value.toFixed(2)}`, 'Close Price']}
                          labelFormatter={(value) => new Date(value).toLocaleDateString()}
                        />
                        <Line
                          type="monotone"
                          dataKey="close"
                          stroke="#10B981"
                          strokeWidth={2}
                          dot={false}
                        />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                ) : (
                  <div className="bg-slate-800/50 rounded-lg p-12 text-center">
                    <BarChart3 className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-white mb-2">No Chart Data</h3>
                    <p className="text-slate-400">Select a symbol and click refresh to load price chart</p>
                  </div>
                )}
              </div>
            )}

            {/* Watchlist Tab */}
            {activeTab === 'watchlist' && (
              <div className="space-y-6">
                <div className="text-center py-12">
                  <Star className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                  <h3 className="text-xl font-semibold text-white mb-2">Watchlist Coming Soon</h3>
                  <p className="text-slate-400">Save your favorite symbols and track them in a personalized watchlist</p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Market Status */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
          <h3 className="text-lg font-semibold text-white mb-4">Market Data Status</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-emerald-400 mb-1">
                {liveData.size}
              </div>
              <div className="text-sm text-slate-400">Symbols Tracked</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-400 mb-1">
                {Array.from(liveData.values()).filter(d => !d.isStale).length}
              </div>
              <div className="text-sm text-slate-400">Fresh Quotes</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-400 mb-1">
                {popularSymbols.length}
              </div>
              <div className="text-sm text-slate-400">Available Symbols</div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default MarketDataPage;