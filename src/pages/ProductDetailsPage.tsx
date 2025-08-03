import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { 
  TrendingUp, 
  ArrowLeft, 
  Edit, 
  Activity,
  Calendar,
  DollarSign,
  Target,
  BarChart3,
  Package,
  AlertTriangle,
  CheckCircle,
  Play,
  Settings
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import { useAuth } from '../hooks/useAuth';
import { productService, Product, ProductStatus } from '../services/productService';
import toast from 'react-hot-toast';

const ProductDetailsPage: React.FC = () => {
  const { user } = useAuth();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'overview' | 'pricing' | 'risk'>('overview');

  useEffect(() => {
    if (id) {
      loadProduct();
    } else {
      setError('No product ID provided');
      setLoading(false);
    }
  }, [id]);

  const loadProduct = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      setError(null);
      const data = await productService.getProduct(id);
      setProduct(data);
    } catch (error: any) {
      console.error('Failed to load product:', error);
      setError('Failed to load product details');
      toast.error('Failed to load product details');
    } finally {
      setLoading(false);
    }
  };

  const handleReprice = async () => {
    if (!product) return;
    
    try {
      await productService.repriceProduct(product.id);
      toast.success('Product repriced successfully');
      loadProduct();
    } catch (error: any) {
      toast.error('Failed to reprice product');
    }
  };

  const handleIssue = async () => {
    if (!product) return;
    
    if (!window.confirm('Are you sure you want to issue this product? This action cannot be undone.')) {
      return;
    }

    try {
      await productService.issueProduct(product.id);
      toast.success('Product issued successfully');
      loadProduct();
    } catch (error: any) {
      toast.error('Failed to issue product');
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
    return `${(value * 100).toFixed(2)}%`;
  };

  const formatGreek = (value?: number) => {
    if (value === undefined || value === null) return 'N/A';
    return value.toFixed(4);
  };

  const getStatusColor = (status: ProductStatus) => {
    switch (status) {
      case ProductStatus.ISSUED:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      case ProductStatus.ACTIVE:
        return 'text-blue-400 bg-blue-500/10 border-blue-500/20';
      case ProductStatus.DRAFT:
        return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
      case ProductStatus.EXPIRED:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
      case ProductStatus.CANCELLED:
        return 'text-red-400 bg-red-500/10 border-red-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
    }
  };

  // Prepare payoff chart data
  const payoffChartData = product?.payoffCurve?.map(point => ({
    spotPrice: point.spotPrice,
    payoff: point.payoffValue,
  })) || [];

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading product details...</p>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <AlertTriangle className="w-16 h-16 text-red-400 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-white mb-2">
            {error || 'Product Not Found'}
          </h2>
          <p className="text-slate-400 mb-6">
            {error || 'The requested product could not be found.'}
          </p>
          <Link
            to="/products"
            className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200"
          >
            Back to Products
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
                <Link to="/products" className="text-slate-400 hover:text-white transition-colors flex items-center">
                  <ArrowLeft className="w-4 h-4 mr-1" />
                  Products
                </Link>
                <span className="text-slate-600">/</span>
                <span className="text-emerald-400 font-medium">Details</span>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <button 
                onClick={handleReprice}
                className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                title="Reprice Product"
              >
                <Activity className="w-5 h-5" />
              </button>
              {product.status === ProductStatus.DRAFT && (
                <Link
                  to={`/products/${product.id}/edit`}
                  className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition-all duration-200"
                  title="Edit Product"
                >
                  <Edit className="w-5 h-5" />
                </Link>
              )}
              <span className="text-sm text-slate-400">{user?.fullName}</span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Product Header */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 mb-8">
          <div className="flex justify-between items-start">
            <div>
              <h2 className="text-2xl font-bold text-white mb-2">{product.name}</h2>
              <div className="flex items-center space-x-6 text-sm text-slate-400">
                <div className="flex items-center">
                  <Package className="w-4 h-4 mr-1" />
                  {product.productType.replace('_', ' ')}
                </div>
                <div className="flex items-center">
                  <BarChart3 className="w-4 h-4 mr-1" />
                  {product.underlyingAsset}
                </div>
                <div className="flex items-center">
                  <Calendar className="w-4 h-4 mr-1" />
                  Maturity: {new Date(product.maturityDate).toLocaleDateString()}
                </div>
              </div>
              {product.description && (
                <p className="text-slate-300 mt-2">{product.description}</p>
              )}
            </div>
            
            <div className="text-right">
              <div className="text-sm text-slate-400 mb-1">Status</div>
              <div className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(product.status)}`}>
                {product.status}
              </div>
              {product.status === ProductStatus.DRAFT && (
                <button
                  onClick={handleIssue}
                  className="mt-2 bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-3 py-1 rounded-md text-sm font-medium transition-all duration-200"
                >
                  Issue Product
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Key Metrics */}
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4 mb-8">
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="flex items-center text-slate-400 text-sm mb-2">
              <DollarSign className="w-4 h-4 mr-1" />
              Notional
            </div>
            <div className="text-xl font-bold text-white">
              {formatCurrency(product.notional)}
            </div>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="text-slate-400 text-sm mb-2">Fair Value</div>
            <div className="text-xl font-bold text-emerald-400">
              {formatCurrency(product.fairValue)}
            </div>
          </div>

          {product.strikePrice && (
            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="flex items-center text-slate-400 text-sm mb-2">
                <Target className="w-4 h-4 mr-1" />
                Strike
              </div>
              <div className="text-xl font-bold text-white">
                {formatCurrency(product.strikePrice)}
              </div>
            </div>
          )}

          {product.payoffRate && (
            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
              <div className="text-slate-400 text-sm mb-2">Payoff Rate</div>
              <div className="text-xl font-bold text-blue-400">
                {formatPercentage(product.payoffRate)}
              </div>
            </div>
          )}

          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="text-slate-400 text-sm mb-2">Implied Vol</div>
            <div className="text-xl font-bold text-purple-400">
              {formatPercentage(product.impliedVolatility)}
            </div>
          </div>

          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-4">
            <div className="text-slate-400 text-sm mb-2">Version</div>
            <div className="text-xl font-bold text-white">
              v{product.currentVersion}
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 mb-8">
          <div className="border-b border-slate-700">
            <nav className="flex space-x-8 px-6">
              {[
                { id: 'overview', label: 'Overview', icon: Package },
                { id: 'pricing', label: 'Pricing & Payoff', icon: BarChart3 },
                { id: 'risk', label: 'Risk Analytics', icon: TrendingUp }
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
              <div className="space-y-6">
                {/* Product Terms */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Product Terms</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <h4 className="text-white font-medium mb-3">Basic Information</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-slate-400">Product Type</span>
                          <span className="text-white">{product.productType.replace('_', ' ')}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-slate-400">Underlying Asset</span>
                          <span className="text-white">{product.underlyingAsset}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-slate-400">Pricing Model</span>
                          <span className="text-white">{product.pricingModel.replace('_', ' ')}</span>
                        </div>
                        {product.linkedStrategyName && (
                          <div className="flex justify-between">
                            <span className="text-slate-400">Linked Strategy</span>
                            <span className="text-emerald-400">{product.linkedStrategyName}</span>
                          </div>
                        )}
                      </div>
                    </div>

                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <h4 className="text-white font-medium mb-3">Financial Terms</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-slate-400">Notional</span>
                          <span className="text-white">{formatCurrency(product.notional)}</span>
                        </div>
                        {product.strikePrice && (
                          <div className="flex justify-between">
                            <span className="text-slate-400">Strike Price</span>
                            <span className="text-white">{formatCurrency(product.strikePrice)}</span>
                          </div>
                        )}
                        {product.barrierLevel && (
                          <div className="flex justify-between">
                            <span className="text-slate-400">Barrier Level</span>
                            <span className="text-white">{formatCurrency(product.barrierLevel)}</span>
                          </div>
                        )}
                        {product.payoffRate && (
                          <div className="flex justify-between">
                            <span className="text-slate-400">Payoff Rate</span>
                            <span className="text-white">{formatPercentage(product.payoffRate)}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Configuration */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Product Configuration</h3>
                  <div className="bg-slate-800/50 rounded-lg p-4">
                    <pre className="text-sm text-slate-300 whitespace-pre-wrap overflow-x-auto">
                      {JSON.stringify(JSON.parse(product.configJson), null, 2)}
                    </pre>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'pricing' && (
              <div className="space-y-8">
                {/* Pricing Summary */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Pricing Summary</h3>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <div className="text-slate-400 text-sm mb-1">Fair Value</div>
                      <div className="text-2xl font-bold text-emerald-400">
                        {formatCurrency(product.fairValue)}
                      </div>
                    </div>
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <div className="text-slate-400 text-sm mb-1">Implied Volatility</div>
                      <div className="text-2xl font-bold text-purple-400">
                        {formatPercentage(product.impliedVolatility)}
                      </div>
                    </div>
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <div className="text-slate-400 text-sm mb-1">Pricing Model</div>
                      <div className="text-lg font-semibold text-white">
                        {product.pricingModel.replace('_', ' ')}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Payoff Diagram */}
                {payoffChartData.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">Payoff Diagram</h3>
                    <div className="bg-slate-800/50 rounded-lg p-4">
                      <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={payoffChartData}>
                          <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                          <XAxis 
                            dataKey="spotPrice" 
                            stroke="#9CA3AF"
                            fontSize={12}
                            tickFormatter={(value) => `$${value}`}
                          />
                          <YAxis 
                            stroke="#9CA3AF"
                            fontSize={12}
                            tickFormatter={(value) => `$${value}`}
                          />
                          <Tooltip 
                            contentStyle={{
                              backgroundColor: '#1F2937',
                              border: '1px solid #374151',
                              borderRadius: '8px',
                              color: '#F9FAFB'
                            }}
                            formatter={(value: any) => [`$${value}`, 'Payoff']}
                            labelFormatter={(value) => `Spot Price: $${value}`}
                          />
                          <Area
                            type="monotone"
                            dataKey="payoff"
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
              </div>
            )}

            {activeTab === 'risk' && (
              <div className="space-y-6">
                {/* Greeks */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Greeks (Risk Sensitivities)</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
                    <div className="bg-slate-800/50 rounded-lg p-4 text-center">
                      <div className="text-slate-400 text-sm mb-1">Delta</div>
                      <div className="text-xl font-bold text-emerald-400">
                        {formatGreek(product.deltaValue)}
                      </div>
                      <div className="text-xs text-slate-500">Price sensitivity</div>
                    </div>

                    <div className="bg-slate-800/50 rounded-lg p-4 text-center">
                      <div className="text-slate-400 text-sm mb-1">Gamma</div>
                      <div className="text-xl font-bold text-blue-400">
                        {formatGreek(product.gammaValue)}
                      </div>
                      <div className="text-xs text-slate-500">Delta sensitivity</div>
                    </div>

                    <div className="bg-slate-800/50 rounded-lg p-4 text-center">
                      <div className="text-slate-400 text-sm mb-1">Theta</div>
                      <div className="text-xl font-bold text-red-400">
                        {formatGreek(product.thetaValue)}
                      </div>
                      <div className="text-xs text-slate-500">Time decay</div>
                    </div>

                    <div className="bg-slate-800/50 rounded-lg p-4 text-center">
                      <div className="text-slate-400 text-sm mb-1">Vega</div>
                      <div className="text-xl font-bold text-purple-400">
                        {formatGreek(product.vegaValue)}
                      </div>
                      <div className="text-xs text-slate-500">Vol sensitivity</div>
                    </div>

                    <div className="bg-slate-800/50 rounded-lg p-4 text-center">
                      <div className="text-slate-400 text-sm mb-1">Rho</div>
                      <div className="text-xl font-bold text-amber-400">
                        {formatGreek(product.rhoValue)}
                      </div>
                      <div className="text-xs text-slate-500">Rate sensitivity</div>
                    </div>
                  </div>
                </div>

                {/* Risk Summary */}
                <div>
                  <h3 className="text-lg font-semibold text-white mb-4">Risk Summary</h3>
                  <div className="bg-slate-800/50 rounded-lg p-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div>
                        <h4 className="text-white font-medium mb-3">Market Risk</h4>
                        <div className="space-y-2 text-sm">
                          <div className="flex justify-between">
                            <span className="text-slate-400">Underlying Exposure</span>
                            <span className="text-white">{product.underlyingAsset}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-slate-400">Delta Exposure</span>
                            <span className="text-white">{formatGreek(product.deltaValue)}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-slate-400">Volatility Risk</span>
                            <span className="text-white">{formatGreek(product.vegaValue)}</span>
                          </div>
                        </div>
                      </div>

                      <div>
                        <h4 className="text-white font-medium mb-3">Time Risk</h4>
                        <div className="space-y-2 text-sm">
                          <div className="flex justify-between">
                            <span className="text-slate-400">Time to Maturity</span>
                            <span className="text-white">
                              {Math.ceil((new Date(product.maturityDate).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24))} days
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-slate-400">Theta (Daily Decay)</span>
                            <span className="text-red-400">{formatGreek(product.thetaValue)}</span>
                          </div>
                        </div>
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

export default ProductDetailsPage;