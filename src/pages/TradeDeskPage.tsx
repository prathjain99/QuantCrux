import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  TrendingUp, 
  Plus, 
  Search, 
  Filter,
  Activity,
  DollarSign,
  Clock,
  Target,
  AlertCircle,
  CheckCircle,
  XCircle,
  Pause,
  BarChart3,
  RefreshCw,
  Eye,
  X
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { 
  tradeService, 
  Order, 
  Trade, 
  Position, 
  MarketQuote,
  OrderRequest,
  OrderSide,
  OrderType,
  OrderStatus,
  InstrumentType,
  TimeInForce
} from '../services/tradeService';
import { portfolioService, Portfolio } from '../services/portfolioService';
import toast from 'react-hot-toast';

const TradeDeskPage: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'orders' | 'trades' | 'positions' | 'quotes'>('orders');
  const [orders, setOrders] = useState<Order[]>([]);
  const [trades, setTrades] = useState<Trade[]>([]);
  const [positions, setPositions] = useState<Position[]>([]);
  const [quotes, setQuotes] = useState<MarketQuote[]>([]);
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [loading, setLoading] = useState(true);
  const [showOrderForm, setShowOrderForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  // Order form state
  const [orderForm, setOrderForm] = useState<OrderRequest>({
    portfolioId: '',
    instrumentType: InstrumentType.ASSET,
    symbol: '',
    side: OrderSide.BUY,
    orderType: OrderType.MARKET,
    quantity: 0,
    timeInForce: TimeInForce.DAY
  });

  useEffect(() => {
    loadData();
    loadPortfolios();
    loadMarketQuotes();
    
    // Auto-refresh every 30 seconds
    const interval = setInterval(() => {
      loadMarketQuotes();
      if (activeTab === 'positions') {
        loadPositions();
      }
    }, 30000);
    
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    switch (activeTab) {
      case 'orders':
        loadOrders();
        break;
      case 'trades':
        loadTrades();
        break;
      case 'positions':
        loadPositions();
        break;
    }
  }, [activeTab]);

  const loadData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        loadOrders(),
        loadTrades(),
        loadPositions()
      ]);
    } catch (error: any) {
      toast.error('Failed to load trade data');
    } finally {
      setLoading(false);
    }
  };

  const loadOrders = async () => {
    try {
      const data = await tradeService.getOrders();
      setOrders(data);
    } catch (error: any) {
      console.error('Failed to load orders:', error);
    }
  };

  const loadTrades = async () => {
    try {
      const data = await tradeService.getTrades();
      setTrades(data);
    } catch (error: any) {
      console.error('Failed to load trades:', error);
    }
  };

  const loadPositions = async () => {
    try {
      const data = await tradeService.getPositions();
      setPositions(data);
    } catch (error: any) {
      console.error('Failed to load positions:', error);
    }
  };

  const loadPortfolios = async () => {
    try {
      const data = await portfolioService.getPortfolios();
      setPortfolios(data);
      if (data.length > 0 && !orderForm.portfolioId) {
        setOrderForm(prev => ({ ...prev, portfolioId: data[0].id }));
      }
    } catch (error: any) {
      console.error('Failed to load portfolios:', error);
    }
  };

  const loadMarketQuotes = async () => {
    try {
      const symbols = ['AAPL', 'MSFT', 'GOOGL', 'TSLA', 'BTCUSD', 'ETHUSD'];
      const data = await tradeService.getMarketQuotes(symbols);
      setQuotes(data);
    } catch (error: any) {
      console.error('Failed to load market quotes:', error);
    }
  };

  const handleCreateOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      await tradeService.createOrder(orderForm);
      toast.success('Order placed successfully');
      setShowOrderForm(false);
      loadOrders();
      loadPositions();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to place order');
    }
  };

  const handleCancelOrder = async (orderId: string) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) {
      return;
    }

    try {
      await tradeService.cancelOrder(orderId);
      toast.success('Order cancelled successfully');
      loadOrders();
    } catch (error: any) {
      toast.error('Failed to cancel order');
    }
  };

  const getOrderStatusColor = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.FILLED:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      case OrderStatus.PENDING:
      case OrderStatus.SUBMITTED:
        return 'text-blue-400 bg-blue-500/10 border-blue-500/20';
      case OrderStatus.PARTIALLY_FILLED:
        return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
      case OrderStatus.CANCELLED:
      case OrderStatus.REJECTED:
      case OrderStatus.EXPIRED:
        return 'text-red-400 bg-red-500/10 border-red-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
    }
  };

  const getOrderStatusIcon = (status: OrderStatus) => {
    switch (status) {
      case OrderStatus.FILLED:
        return <CheckCircle className="w-3 h-3" />;
      case OrderStatus.PENDING:
      case OrderStatus.SUBMITTED:
        return <Clock className="w-3 h-3" />;
      case OrderStatus.PARTIALLY_FILLED:
        return <Pause className="w-3 h-3" />;
      case OrderStatus.CANCELLED:
      case OrderStatus.REJECTED:
      case OrderStatus.EXPIRED:
        return <XCircle className="w-3 h-3" />;
      default:
        return <AlertCircle className="w-3 h-3" />;
    }
  };

  const formatCurrency = (value?: number) => {
    if (value === undefined || value === null) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
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

  const filteredData = () => {
    const term = searchTerm.toLowerCase();
    switch (activeTab) {
      case 'orders':
        return orders.filter(order => 
          order.symbol.toLowerCase().includes(term) ||
          order.portfolioName.toLowerCase().includes(term)
        );
      case 'trades':
        return trades.filter(trade => 
          trade.symbol.toLowerCase().includes(term) ||
          trade.portfolioName.toLowerCase().includes(term)
        );
      case 'positions':
        return positions.filter(position => 
          position.symbol.toLowerCase().includes(term) ||
          position.portfolioName.toLowerCase().includes(term)
        );
      case 'quotes':
        return quotes.filter(quote => 
          quote.symbol.toLowerCase().includes(term)
        );
      default:
        return [];
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading trade desk...</p>
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
                <span className="text-emerald-400 font-medium">Trade Desk</span>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <button
                onClick={loadMarketQuotes}
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                title="Refresh Quotes"
              >
                <RefreshCw className="w-5 h-5" />
              </button>
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
            <h2 className="text-3xl font-bold text-white mb-2">Trade Desk</h2>
            <p className="text-slate-400">Execute trades and manage positions across your portfolios</p>
          </div>
          
          <button
            onClick={() => setShowOrderForm(true)}
            className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 flex items-center"
          >
            <Plus className="w-5 h-5 mr-2" />
            New Order
          </button>
        </div>

        {/* Market Overview */}
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
          {quotes.slice(0, 6).map((quote) => (
            <div key={quote.symbol} className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="flex justify-between items-start mb-2">
                <h3 className="text-sm font-semibold text-white">{quote.symbol}</h3>
                <div className={`text-xs px-2 py-1 rounded ${
                  quote.trend === 'UP' ? 'bg-emerald-500/20 text-emerald-400' :
                  quote.trend === 'DOWN' ? 'bg-red-500/20 text-red-400' :
                  'bg-slate-500/20 text-slate-400'
                }`}>
                  {quote.trend}
                </div>
              </div>
              <div className="text-lg font-bold text-white mb-1">
                {formatCurrency(quote.lastPrice)}
              </div>
              <div className={`text-sm ${getReturnColor(quote.dayChangePercent)}`}>
                {formatCurrency(quote.dayChange)} ({formatPercentage(quote.dayChangePercent)})
              </div>
            </div>
          ))}
        </div>

        {/* Filters */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 mb-8">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  type="text"
                  placeholder="Search by symbol or portfolio..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                />
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 mb-8">
          <div className="border-b border-slate-700">
            <nav className="flex space-x-8 px-6">
              {[
                { id: 'orders', label: 'Orders', icon: Target, count: orders.length },
                { id: 'trades', label: 'Trades', icon: Activity, count: trades.length },
                { id: 'positions', label: 'Positions', icon: BarChart3, count: positions.filter(p => p.netQuantity !== 0).length },
                { id: 'quotes', label: 'Market Data', icon: TrendingUp, count: quotes.length }
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
                  <span className="ml-2 px-2 py-1 bg-slate-700 rounded-full text-xs">
                    {tab.count}
                  </span>
                </button>
              ))}
            </nav>
          </div>

          <div className="p-6">
            {/* Orders Tab */}
            {activeTab === 'orders' && (
              <div>
                {filteredData().length === 0 ? (
                  <div className="text-center py-8">
                    <Target className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-white mb-2">No orders found</h3>
                    <p className="text-slate-400 mb-6">
                      {searchTerm ? 'Try adjusting your search criteria' : 'Place your first order to get started'}
                    </p>
                    {!searchTerm && (
                      <button
                        onClick={() => setShowOrderForm(true)}
                        className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200"
                      >
                        Place Order
                      </button>
                    )}
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead className="bg-slate-800/50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Symbol</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Side</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Type</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Quantity</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Price</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Status</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Created</th>
                          <th className="px-4 py-3 text-center text-xs font-medium text-slate-300 uppercase">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-700">
                        {(filteredData() as Order[]).map((order) => (
                          <tr key={order.id} className="hover:bg-slate-800/30">
                            <td className="px-4 py-3">
                              <div className="text-sm font-medium text-white">{order.symbol}</div>
                              <div className="text-xs text-slate-400">{order.portfolioName}</div>
                            </td>
                            <td className="px-4 py-3">
                              <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                                order.side === OrderSide.BUY ? 'bg-emerald-500/20 text-emerald-400' : 'bg-red-500/20 text-red-400'
                              }`}>
                                {order.side}
                              </span>
                            </td>
                            <td className="px-4 py-3 text-sm text-white">{order.orderType}</td>
                            <td className="px-4 py-3 text-right">
                              <div className="text-sm text-white">{order.quantity.toLocaleString()}</div>
                              <div className="text-xs text-slate-400">
                                Filled: {order.filledQuantity.toLocaleString()}
                              </div>
                            </td>
                            <td className="px-4 py-3 text-right text-sm text-white">
                              {order.limitPrice ? formatCurrency(order.limitPrice) : 'Market'}
                            </td>
                            <td className="px-4 py-3">
                              <div className={`inline-flex items-center px-2 py-1 rounded-full border text-xs font-medium ${getOrderStatusColor(order.status)}`}>
                                {getOrderStatusIcon(order.status)}
                                <span className="ml-1">{order.status}</span>
                              </div>
                            </td>
                            <td className="px-4 py-3 text-sm text-slate-300">
                              {new Date(order.createdAt).toLocaleString()}
                            </td>
                            <td className="px-4 py-3 text-center">
                              {(order.status === OrderStatus.PENDING || order.status === OrderStatus.SUBMITTED) && (
                                <button
                                  onClick={() => handleCancelOrder(order.id)}
                                  className="text-red-400 hover:text-red-300 transition-colors"
                                  title="Cancel Order"
                                >
                                  <X className="w-4 h-4" />
                                </button>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}

            {/* Trades Tab */}
            {activeTab === 'trades' && (
              <div>
                {filteredData().length === 0 ? (
                  <div className="text-center py-8">
                    <Activity className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-white mb-2">No trades found</h3>
                    <p className="text-slate-400">
                      {searchTerm ? 'Try adjusting your search criteria' : 'Your executed trades will appear here'}
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead className="bg-slate-800/50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Symbol</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Side</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Quantity</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Price</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Amount</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Quality</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Executed</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-700">
                        {(filteredData() as Trade[]).map((trade) => (
                          <tr key={trade.id} className="hover:bg-slate-800/30">
                            <td className="px-4 py-3">
                              <div className="text-sm font-medium text-white">{trade.symbol}</div>
                              <div className="text-xs text-slate-400">{trade.portfolioName}</div>
                            </td>
                            <td className="px-4 py-3">
                              <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                                trade.side === OrderSide.BUY ? 'bg-emerald-500/20 text-emerald-400' : 'bg-red-500/20 text-red-400'
                              }`}>
                                {trade.side}
                              </span>
                            </td>
                            <td className="px-4 py-3 text-right text-sm text-white">
                              {trade.quantity.toLocaleString()}
                            </td>
                            <td className="px-4 py-3 text-right text-sm text-white">
                              {formatCurrency(trade.price)}
                            </td>
                            <td className="px-4 py-3 text-right">
                              <div className="text-sm text-white">{formatCurrency(trade.totalAmount)}</div>
                              <div className="text-xs text-slate-400">
                                Fees: {formatCurrency(trade.fees)}
                              </div>
                            </td>
                            <td className="px-4 py-3">
                              <span className={`text-xs px-2 py-1 rounded ${
                                trade.executionQuality === 'Excellent' ? 'bg-emerald-500/20 text-emerald-400' :
                                trade.executionQuality === 'Good' ? 'bg-blue-500/20 text-blue-400' :
                                trade.executionQuality === 'Fair' ? 'bg-amber-500/20 text-amber-400' :
                                'bg-red-500/20 text-red-400'
                              }`}>
                                {trade.executionQuality || 'N/A'}
                              </span>
                            </td>
                            <td className="px-4 py-3 text-sm text-slate-300">
                              {new Date(trade.executedAt).toLocaleString()}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}

            {/* Positions Tab */}
            {activeTab === 'positions' && (
              <div>
                {filteredData().length === 0 ? (
                  <div className="text-center py-8">
                    <BarChart3 className="w-16 h-16 text-slate-600 mx-auto mb-4" />
                    <h3 className="text-xl font-semibold text-white mb-2">No positions found</h3>
                    <p className="text-slate-400">
                      {searchTerm ? 'Try adjusting your search criteria' : 'Your open positions will appear here'}
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead className="bg-slate-800/50">
                        <tr>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Symbol</th>
                          <th className="px-4 py-3 text-left text-xs font-medium text-slate-300 uppercase">Type</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Quantity</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Avg Price</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Market Value</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">P&L</th>
                          <th className="px-4 py-3 text-right text-xs font-medium text-slate-300 uppercase">Return</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-700">
                        {(filteredData() as Position[]).filter(p => p.netQuantity !== 0).map((position) => (
                          <tr key={position.id} className="hover:bg-slate-800/30">
                            <td className="px-4 py-3">
                              <div className="text-sm font-medium text-white">{position.symbol}</div>
                              <div className="text-xs text-slate-400">{position.portfolioName}</div>
                            </td>
                            <td className="px-4 py-3">
                              <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                                position.positionType === 'Long' ? 'bg-emerald-500/20 text-emerald-400' :
                                position.positionType === 'Short' ? 'bg-red-500/20 text-red-400' :
                                'bg-slate-500/20 text-slate-400'
                              }`}>
                                {position.positionType}
                              </span>
                            </td>
                            <td className="px-4 py-3 text-right text-sm text-white">
                              {position.netQuantity.toLocaleString()}
                            </td>
                            <td className="px-4 py-3 text-right text-sm text-white">
                              {formatCurrency(position.avgPrice)}
                            </td>
                            <td className="px-4 py-3 text-right text-sm text-white">
                              {formatCurrency(position.marketValue)}
                            </td>
                            <td className={`px-4 py-3 text-right text-sm font-medium ${getReturnColor(position.unrealizedPnl)}`}>
                              {formatCurrency(position.unrealizedPnl)}
                            </td>
                            <td className={`px-4 py-3 text-right text-sm font-medium ${getReturnColor(position.returnPercent)}`}>
                              {formatPercentage(position.returnPercent)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}

            {/* Market Data Tab */}
            {activeTab === 'quotes' && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {(filteredData() as MarketQuote[]).map((quote) => (
                  <div key={quote.symbol} className="bg-slate-800/50 rounded-xl border border-slate-700 p-6">
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <h3 className="text-lg font-semibold text-white">{quote.symbol}</h3>
                        <p className="text-sm text-slate-400">{quote.instrumentType}</p>
                      </div>
                      <div className={`px-2 py-1 rounded text-xs font-medium ${
                        quote.trend === 'UP' ? 'bg-emerald-500/20 text-emerald-400' :
                        quote.trend === 'DOWN' ? 'bg-red-500/20 text-red-400' :
                        'bg-slate-500/20 text-slate-400'
                      }`}>
                        {quote.trend}
                      </div>
                    </div>

                    <div className="space-y-3">
                      <div className="text-center">
                        <div className="text-2xl font-bold text-white mb-1">
                          {formatCurrency(quote.lastPrice)}
                        </div>
                        <div className={`text-sm ${getReturnColor(quote.dayChangePercent)}`}>
                          {formatCurrency(quote.dayChange)} ({formatPercentage(quote.dayChangePercent)})
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-3 text-xs">
                        <div className="bg-slate-700/50 rounded p-2">
                          <div className="text-slate-400">Bid</div>
                          <div className="text-white font-medium">{formatCurrency(quote.bidPrice)}</div>
                        </div>
                        <div className="bg-slate-700/50 rounded p-2">
                          <div className="text-slate-400">Ask</div>
                          <div className="text-white font-medium">{formatCurrency(quote.askPrice)}</div>
                        </div>
                        <div className="bg-slate-700/50 rounded p-2">
                          <div className="text-slate-400">High</div>
                          <div className="text-white font-medium">{formatCurrency(quote.highPrice)}</div>
                        </div>
                        <div className="bg-slate-700/50 rounded p-2">
                          <div className="text-slate-400">Low</div>
                          <div className="text-white font-medium">{formatCurrency(quote.lowPrice)}</div>
                        </div>
                      </div>

                      <div className="text-xs text-slate-500 text-center">
                        Updated: {new Date(quote.updatedAt).toLocaleTimeString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Order Form Modal */}
        {showOrderForm && (
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-slate-900 rounded-xl border border-slate-700 w-full max-w-2xl">
              <div className="flex items-center justify-between p-6 border-b border-slate-700">
                <h2 className="text-xl font-semibold text-white">Place Order</h2>
                <button
                  onClick={() => setShowOrderForm(false)}
                  className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <form onSubmit={handleCreateOrder} className="p-6 space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Portfolio
                    </label>
                    <select
                      value={orderForm.portfolioId}
                      onChange={(e) => setOrderForm(prev => ({ ...prev, portfolioId: e.target.value }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      required
                    >
                      <option value="">Select Portfolio</option>
                      {portfolios.map((portfolio) => (
                        <option key={portfolio.id} value={portfolio.id}>
                          {portfolio.name} (${portfolio.cashBalance.toLocaleString()} cash)
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Symbol
                    </label>
                    <input
                      type="text"
                      value={orderForm.symbol}
                      onChange={(e) => setOrderForm(prev => ({ ...prev, symbol: e.target.value.toUpperCase() }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="e.g., AAPL"
                      required
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Side
                    </label>
                    <select
                      value={orderForm.side}
                      onChange={(e) => setOrderForm(prev => ({ ...prev, side: e.target.value as OrderSide }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value={OrderSide.BUY}>Buy</option>
                      <option value={OrderSide.SELL}>Sell</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Order Type
                    </label>
                    <select
                      value={orderForm.orderType}
                      onChange={(e) => setOrderForm(prev => ({ ...prev, orderType: e.target.value as OrderType }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value={OrderType.MARKET}>Market</option>
                      <option value={OrderType.LIMIT}>Limit</option>
                      <option value={OrderType.STOP}>Stop</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Quantity
                    </label>
                    <input
                      type="number"
                      value={orderForm.quantity}
                      onChange={(e) => setOrderForm(prev => ({ ...prev, quantity: parseFloat(e.target.value) || 0 }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="100"
                      min="0.01"
                      step="0.01"
                      required
                    />
                  </div>
                </div>

                {orderForm.orderType === OrderType.LIMIT && (
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Limit Price
                    </label>
                    <input
                      type="number"
                      value={orderForm.limitPrice || ''}
                      onChange={(e) => setOrderForm(prev => ({ ...prev, limitPrice: parseFloat(e.target.value) || undefined }))}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="150.00"
                      step="0.01"
                    />
                  </div>
                )}

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Notes (Optional)
                  </label>
                  <textarea
                    value={orderForm.notes || ''}
                    onChange={(e) => setOrderForm(prev => ({ ...prev, notes: e.target.value }))}
                    rows={3}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    placeholder="Order notes..."
                  />
                </div>

                <div className="flex justify-end space-x-4 pt-4 border-t border-slate-700">
                  <button
                    type="button"
                    onClick={() => setShowOrderForm(false)}
                    className="px-6 py-3 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105"
                  >
                    Place Order
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

export default TradeDeskPage;