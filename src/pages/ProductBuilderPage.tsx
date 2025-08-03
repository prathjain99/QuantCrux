import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { 
  Save, 
  TrendingUp, 
  ArrowLeft, 
  Settings, 
  DollarSign,
  Calendar,
  Activity,
  BarChart3,
  Package,
  Calculator,
  Target,
  AlertCircle
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { productService, ProductRequest, ProductType, ProductStatus, PricingModel } from '../services/productService';
import { strategyService, Strategy } from '../services/strategyService';
import toast from 'react-hot-toast';

const ProductBuilderPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = !!id;

  const [formData, setFormData] = useState<ProductRequest>({
    name: '',
    description: '',
    productType: ProductType.DIGITAL_OPTION,
    underlyingAsset: 'AAPL',
    notional: 100000,
    maturityDate: '2025-12-31',
    configJson: JSON.stringify({
      condition: "strategy_return > 5%",
      payoff: "12%",
      barrier_monitoring: "continuous",
      settlement: "cash"
    }, null, 2),
    pricingModel: PricingModel.MONTE_CARLO,
    status: ProductStatus.DRAFT,
    simulationRuns: 10000,
    riskFreeRate: 0.05,
    impliedVolatility: 0.20
  });

  const [strategies, setStrategies] = useState<Strategy[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingStrategies, setLoadingStrategies] = useState(true);

  useEffect(() => {
    loadStrategies();
    if (isEditing) {
      loadProduct();
    }
  }, [id]);

  const loadStrategies = async () => {
    try {
      setLoadingStrategies(true);
      const data = await strategyService.getStrategies();
      setStrategies(data);
    } catch (error: any) {
      console.error('Failed to load strategies:', error);
    } finally {
      setLoadingStrategies(false);
    }
  };

  const loadProduct = async () => {
    if (!id) return;
    
    try {
      setLoading(true);
      const product = await productService.getProduct(id);
      setFormData({
        name: product.name,
        description: product.description || '',
        productType: product.productType,
        underlyingAsset: product.underlyingAsset,
        linkedStrategyId: product.linkedStrategyId,
        notional: product.notional,
        strikePrice: product.strikePrice,
        barrierLevel: product.barrierLevel,
        payoffRate: product.payoffRate,
        issueDate: product.issueDate,
        maturityDate: product.maturityDate,
        settlementDate: product.settlementDate,
        configJson: product.configJson,
        pricingModel: product.pricingModel,
        status: product.status,
        simulationRuns: 10000,
        riskFreeRate: 0.05,
        impliedVolatility: 0.20
      });
    } catch (error: any) {
      toast.error('Failed to load product');
      navigate('/products');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: (() => {
        // Handle optional UUID fields
        if (name === 'linkedStrategyId') {
          return value === '' ? undefined : value;
        }
        
        // Handle optional number fields
        if (['strikePrice', 'barrierLevel', 'simulationRuns'].includes(name)) {
          if (value === '') return undefined;
          const numValue = parseFloat(value);
          return isNaN(numValue) ? undefined : numValue;
        }
        
        // Handle required number fields
        if (name === 'notional') {
          const numValue = parseFloat(value);
          return isNaN(numValue) ? 100000 : numValue; // Default to 100000 if invalid
        }
        
        // Handle optional date fields
        if (['issueDate', 'settlementDate'].includes(name)) {
          return value === '' ? undefined : value;
        }
        
        // Handle all other fields as strings
        return value;
      })()
    }));
  };

  const handleConfigChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setFormData(prev => ({
      ...prev,
      configJson: e.target.value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      
      // Validate JSON
      JSON.parse(formData.configJson);
      
      if (isEditing && id) {
        await productService.updateProduct(id, formData);
        toast.success('Product updated successfully');
      } else {
        await productService.createProduct(formData);
        toast.success('Product created successfully');
      }
      
      navigate('/products');
    } catch (error: any) {
      if (error instanceof SyntaxError) {
        toast.error('Invalid JSON configuration');
      } else {
        toast.error(error.response?.data?.message || 'Failed to save product');
      }
    } finally {
      setLoading(false);
    }
  };

  const getProductTypeDescription = (type: ProductType) => {
    switch (type) {
      case ProductType.DIGITAL_OPTION:
        return 'Pays fixed amount if condition is met';
      case ProductType.BARRIER_OPTION:
        return 'Knock-in/knock-out based on barrier level';
      case ProductType.STRATEGY_LINKED_NOTE:
        return 'Payoff linked to strategy performance';
      case ProductType.DUAL_CURRENCY:
        return 'Multi-currency structured product';
      default:
        return 'Custom structured product';
    }
  };

  if (loading && isEditing) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading product...</p>
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
                <span className="text-emerald-400 font-medium">
                  {isEditing ? 'Edit Product' : 'New Product'}
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
          {/* Product Form */}
          <div className="lg:col-span-2">
            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
              <div className="flex items-center mb-6">
                <Package className="w-6 h-6 text-emerald-400 mr-3" />
                <h2 className="text-xl font-semibold text-white">Product Configuration</h2>
              </div>

              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Basic Info */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Product Name
                    </label>
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="Enter product name"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Product Type
                    </label>
                    <select
                      name="productType"
                      value={formData.productType}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value={ProductType.DIGITAL_OPTION}>Digital Option</option>
                      <option value={ProductType.BARRIER_OPTION}>Barrier Option</option>
                      <option value={ProductType.STRATEGY_LINKED_NOTE}>Strategy-Linked Note</option>
                      <option value={ProductType.DUAL_CURRENCY}>Dual Currency</option>
                      <option value={ProductType.CUSTOM_PAYOFF}>Custom Payoff</option>
                    </select>
                    <p className="text-xs text-slate-500 mt-1">
                      {getProductTypeDescription(formData.productType)}
                    </p>
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
                    placeholder="Describe your product..."
                  />
                </div>

                {/* Underlying & Strategy */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Underlying Asset
                    </label>
                    <input
                      type="text"
                      name="underlyingAsset"
                      value={formData.underlyingAsset}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="e.g., AAPL, BTCUSD"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Linked Strategy (Optional)
                    </label>
                    <select
                      name="linkedStrategyId"
                      value={formData.linkedStrategyId || ''}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value="">No strategy link</option>
                      {strategies.map((strategy) => (
                        <option key={strategy.id} value={strategy.id}>
                          {strategy.name} ({strategy.symbol})
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Financial Terms */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Notional Amount
                    </label>
                    <div className="relative">
                      <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                      <input
                        type="number"
                        name="notional"
                        value={formData.notional}
                        onChange={handleInputChange}
                        className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                        placeholder="100000"
                        min="1000"
                        step="1000"
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Strike Price (Optional)
                    </label>
                    <div className="relative">
                      <Target className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                      <input
                        type="number"
                        name="strikePrice"
                        value={formData.strikePrice ?? ''}
                        onChange={handleInputChange}
                        className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                        placeholder="150.00"
                        step="0.01"
                      />
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Barrier Level (Optional)
                    </label>
                    <input
                      type="number"
                      name="barrierLevel"
                      value={formData.barrierLevel ?? ''}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      placeholder="140.00"
                      step="0.01"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Payoff Rate (%)
                    </label>
                    <input
                      type="number"
                      name="payoffRate"
                      value={formData.payoffRate ? formData.payoffRate * 100 : ''}
                      onChange={(e) => {
                        const value = parseFloat(e.target.value) / 100;
                        setFormData(prev => ({
                          ...prev,
                          payoffRate: isNaN(value) ? undefined : value
                        }));
                      }}
                      value={formData.settlementDate ?? ''}
                      placeholder="12.0"
                      step="0.1"
                    />
                  </div>
                </div>

                {/* Dates */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Issue Date (Optional)
                    </label>
                    <div className="relative">
                      <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                      <input
                        type="date"
                        name="issueDate"
                        value={formData.issueDate ?? ''}
                        onChange={handleInputChange}
                        className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Maturity Date
                    </label>
                    <div className="relative">
                      <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                      <input
                        type="date"
                        name="maturityDate"
                        value={formData.maturityDate}
                        onChange={handleInputChange}
                        className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                        required
                      />
                    </div>
                  </div>
                </div>

                {/* Pricing Model */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">
                      Pricing Model
                    </label>
                    <select
                      name="pricingModel"
                      value={formData.pricingModel}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    >
                      <option value={PricingModel.MONTE_CARLO}>Monte Carlo</option>
                      <option value={PricingModel.BLACK_SCHOLES}>Black-Scholes</option>
                      <option value={PricingModel.BINOMIAL_TREE}>Binomial Tree</option>
                      <option value={PricingModel.CUSTOM}>Custom</option>
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
                      <option value={ProductStatus.DRAFT}>Draft</option>
                      <option value={ProductStatus.ISSUED}>Issued</option>
                      <option value={ProductStatus.ACTIVE}>Active</option>
                      <option value={ProductStatus.CANCELLED}>Cancelled</option>
                    </select>
                  </div>
                </div>

                {/* Product Configuration JSON */}
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Product Configuration (JSON)
                  </label>
                  <textarea
                    value={formData.configJson}
                    onChange={handleConfigChange}
                    rows={12}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200 font-mono text-sm"
                    placeholder="Enter product configuration as JSON..."
                    required
                  />
                </div>

                {/* Actions */}
                <div className="flex justify-end">
                  <button
                    type="submit"
                    disabled={loading}
                    className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 disabled:opacity-50 disabled:transform-none flex items-center"
                  >
                    <Save className="w-5 h-5 mr-2" />
                    {loading ? 'Saving...' : (isEditing ? 'Update Product' : 'Create Product')}
                  </button>
                </div>
              </form>
            </div>
          </div>

          {/* Pricing Parameters */}
          <div className="lg:col-span-1">
            <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6">
              <div className="flex items-center mb-6">
                <Calculator className="w-6 h-6 text-blue-400 mr-3" />
                <h2 className="text-xl font-semibold text-white">Pricing Parameters</h2>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Simulation Runs
                  </label>
                  <input
                    type="number"
                    name="simulationRuns"
                    value={formData.simulationRuns}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    min="1000"
                    max="100000"
                    step="1000"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Risk-Free Rate (%)
                  </label>
                  <input
                    type="number"
                    name="riskFreeRate"
                    value={formData.riskFreeRate ? formData.riskFreeRate * 100 : ''}
                    onChange={(e) => {
                      const value = parseFloat(e.target.value) / 100;
                      setFormData(prev => ({
                        ...prev,
                        riskFreeRate: isNaN(value) ? 0.05 : value
                      }));
                    }}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    step="0.1"
                    min="0"
                    max="20"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">
                    Implied Volatility (%)
                  </label>
                  <input
                    type="number"
                    name="impliedVolatility"
                    value={formData.impliedVolatility ? formData.impliedVolatility * 100 : ''}
                    onChange={(e) => {
                      const value = parseFloat(e.target.value) / 100;
                      setFormData(prev => ({
                        ...prev,
                        impliedVolatility: isNaN(value) ? 0.20 : value
                      }));
                    }}
                    className="w-full px-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                    step="0.1"
                    min="1"
                    max="100"
                  />
                </div>
              </div>

              {/* Product Preview */}
              <div className="mt-6 p-4 bg-slate-800/30 rounded-lg">
                <h4 className="text-sm font-medium text-slate-300 mb-3">Product Preview</h4>
                <div className="text-xs text-slate-400 space-y-1">
                  <div>Type: {formData.productType}</div>
                  <div>Underlying: {formData.underlyingAsset}</div>
                  <div>Notional: ${formData.notional?.toLocaleString()}</div>
                  <div>Maturity: {formData.maturityDate}</div>
                  {formData.payoffRate && (
                    <div>Payoff Rate: {(formData.payoffRate * 100).toFixed(1)}%</div>
                  )}
                  <div>Model: {formData.pricingModel}</div>
                </div>
              </div>

              {/* Pricing Info */}
              <div className="mt-4 p-3 bg-blue-500/10 border border-blue-500/20 rounded-lg">
                <div className="flex items-center text-blue-400 text-sm mb-1">
                  <AlertCircle className="w-4 h-4 mr-1" />
                  Pricing Information
                </div>
                <p className="text-xs text-slate-400">
                  Product will be priced using {formData.pricingModel} model with {formData.simulationRuns?.toLocaleString()} simulations.
                  Greeks will be calculated automatically.
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default ProductBuilderPage;