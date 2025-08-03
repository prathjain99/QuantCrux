import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { 
  Plus, 
  Search, 
  Filter, 
  TrendingUp, 
  Package, 
  Clock, 
  DollarSign,
  Activity,
  Edit,
  Trash2,
  Eye,
  Play,
  CheckCircle,
  XCircle,
  AlertCircle,
  Calendar,
  BarChart3
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { productService, Product, ProductStatus, ProductType } from '../services/productService';
import toast from 'react-hot-toast';

const ProductsPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<ProductStatus | 'ALL'>('ALL');
  const [typeFilter, setTypeFilter] = useState<ProductType | 'ALL'>('ALL');

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getProducts();
      setProducts(data);
    } catch (error: any) {
      toast.error('Failed to load products');
      console.error('Error loading products:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteProduct = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this product?')) {
      return;
    }

    try {
      await productService.deleteProduct(id);
      toast.success('Product deleted successfully');
      loadProducts();
    } catch (error: any) {
      toast.error('Failed to delete product');
    }
  };

  const handleIssueProduct = async (id: string) => {
    if (!window.confirm('Are you sure you want to issue this product? This action cannot be undone.')) {
      return;
    }

    try {
      await productService.issueProduct(id);
      toast.success('Product issued successfully');
      loadProducts();
    } catch (error: any) {
      toast.error('Failed to issue product');
    }
  };

  const handleRepriceProduct = async (id: string) => {
    try {
      await productService.repriceProduct(id);
      toast.success('Product repriced successfully');
      loadProducts();
    } catch (error: any) {
      toast.error('Failed to reprice product');
    }
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

  const getStatusIcon = (status: ProductStatus) => {
    switch (status) {
      case ProductStatus.ISSUED:
        return <CheckCircle className="w-3 h-3" />;
      case ProductStatus.ACTIVE:
        return <Play className="w-3 h-3" />;
      case ProductStatus.DRAFT:
        return <Edit className="w-3 h-3" />;
      case ProductStatus.EXPIRED:
        return <Clock className="w-3 h-3" />;
      case ProductStatus.CANCELLED:
        return <XCircle className="w-3 h-3" />;
      default:
        return <AlertCircle className="w-3 h-3" />;
    }
  };

  const getProductTypeDisplay = (type: ProductType) => {
    switch (type) {
      case ProductType.DIGITAL_OPTION:
        return 'Digital Option';
      case ProductType.BARRIER_OPTION:
        return 'Barrier Option';
      case ProductType.KNOCK_IN_OPTION:
        return 'Knock-In Option';
      case ProductType.KNOCK_OUT_OPTION:
        return 'Knock-Out Option';
      case ProductType.DUAL_CURRENCY:
        return 'Dual Currency';
      case ProductType.STRATEGY_LINKED_NOTE:
        return 'Strategy-Linked Note';
      case ProductType.CUSTOM_PAYOFF:
        return 'Custom Payoff';
      default:
        return type;
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

  const filteredProducts = products.filter(product => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         product.underlyingAsset.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (product.linkedStrategyName && product.linkedStrategyName.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesStatus = statusFilter === 'ALL' || product.status === statusFilter;
    const matchesType = typeFilter === 'ALL' || product.productType === typeFilter;
    return matchesSearch && matchesStatus && matchesType;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
        <div className="text-center">
          <div className="w-8 h-8 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-slate-400">Loading products...</p>
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
                <span className="text-emerald-400 font-medium">Products</span>
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
            <h2 className="text-3xl font-bold text-white mb-2">Product Builder</h2>
            <p className="text-slate-400">Create and manage structured financial products</p>
          </div>
          
          <Link
            to="/products/new"
            className="bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105 flex items-center"
          >
            <Plus className="w-5 h-5 mr-2" />
            New Product
          </Link>
        </div>

        {/* Filters */}
        <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 mb-8">
          <div className="flex flex-col lg:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  type="text"
                  placeholder="Search products..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200"
                />
              </div>
            </div>
            
            <div className="lg:w-48">
              <div className="relative">
                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value as ProductStatus | 'ALL')}
                  className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200 appearance-none"
                >
                  <option value="ALL">All Status</option>
                  <option value={ProductStatus.DRAFT}>Draft</option>
                  <option value={ProductStatus.ISSUED}>Issued</option>
                  <option value={ProductStatus.ACTIVE}>Active</option>
                  <option value={ProductStatus.EXPIRED}>Expired</option>
                  <option value={ProductStatus.CANCELLED}>Cancelled</option>
                </select>
              </div>
            </div>

            <div className="lg:w-48">
              <div className="relative">
                <Package className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
                <select
                  value={typeFilter}
                  onChange={(e) => setTypeFilter(e.target.value as ProductType | 'ALL')}
                  className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all duration-200 appearance-none"
                >
                  <option value="ALL">All Types</option>
                  <option value={ProductType.DIGITAL_OPTION}>Digital Option</option>
                  <option value={ProductType.BARRIER_OPTION}>Barrier Option</option>
                  <option value={ProductType.STRATEGY_LINKED_NOTE}>Strategy-Linked Note</option>
                  <option value={ProductType.DUAL_CURRENCY}>Dual Currency</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Products Grid */}
        {filteredProducts.length === 0 ? (
          <div className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-12 text-center">
            <Package className="w-16 h-16 text-slate-600 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-white mb-2">No products found</h3>
            <p className="text-slate-400 mb-6">
              {searchTerm || statusFilter !== 'ALL' || typeFilter !== 'ALL'
                ? 'Try adjusting your search or filter criteria'
                : 'Get started by creating your first structured product'
              }
            </p>
            {!searchTerm && statusFilter === 'ALL' && typeFilter === 'ALL' && (
              <Link
                to="/products/new"
                className="inline-flex items-center bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 transform hover:scale-105"
              >
                <Plus className="w-5 h-5 mr-2" />
                Create Product
              </Link>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
            {filteredProducts.map((product) => (
              <div
                key={product.id}
                className="bg-slate-900/50 backdrop-blur-sm rounded-xl border border-slate-700 p-6 hover:border-emerald-500/50 transition-all duration-300 group"
              >
                {/* Product Header */}
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-white mb-1 group-hover:text-emerald-400 transition-colors">
                      {product.name}
                    </h3>
                    <p className="text-sm text-slate-400 mb-2">
                      {getProductTypeDisplay(product.productType)} • {product.underlyingAsset}
                    </p>
                    {product.description && (
                      <p className="text-sm text-slate-500 line-clamp-2">{product.description}</p>
                    )}
                  </div>
                  
                  <div className={`px-2 py-1 rounded-full border text-xs font-medium flex items-center ${getStatusColor(product.status)}`}>
                    {getStatusIcon(product.status)}
                    <span className="ml-1">{product.status}</span>
                  </div>
                </div>

                {/* Product Details */}
                <div className="space-y-3 mb-4">
                  <div className="grid grid-cols-2 gap-3">
                    <div className="bg-slate-800/50 rounded-lg p-3">
                      <div className="flex items-center text-xs text-slate-400 mb-1">
                        <DollarSign className="w-3 h-3 mr-1" />
                        Notional
                      </div>
                      <div className="text-sm font-semibold text-white">
                        {formatCurrency(product.notional)}
                      </div>
                    </div>
                    
                    <div className="bg-slate-800/50 rounded-lg p-3">
                      <div className="text-xs text-slate-400 mb-1">Fair Value</div>
                      <div className="text-sm font-semibold text-emerald-400">
                        {formatCurrency(product.fairValue)}
                      </div>
                    </div>
                  </div>

                  {product.payoffRate && (
                    <div className="bg-slate-800/50 rounded-lg p-3">
                      <div className="text-xs text-slate-400 mb-1">Payoff Rate</div>
                      <div className="text-sm font-semibold text-blue-400">
                        {formatPercentage(product.payoffRate)}
                      </div>
                    </div>
                  )}

                  <div className="flex items-center justify-between text-xs text-slate-400">
                    <div className="flex items-center">
                      <Calendar className="w-3 h-3 mr-1" />
                      Maturity: {new Date(product.maturityDate).toLocaleDateString()}
                    </div>
                    {product.linkedStrategyName && (
                      <div className="flex items-center">
                        <BarChart3 className="w-3 h-3 mr-1" />
                        {product.linkedStrategyName}
                      </div>
                    )}
                  </div>
                </div>

                {/* Greeks (if available) */}
                {product.deltaValue && (
                  <div className="grid grid-cols-3 gap-2 text-xs mb-4">
                    <div className="text-center">
                      <div className="text-slate-400">Delta</div>
                      <div className="text-white font-medium">{product.deltaValue?.toFixed(3)}</div>
                    </div>
                    <div className="text-center">
                      <div className="text-slate-400">Gamma</div>
                      <div className="text-white font-medium">{product.gammaValue?.toFixed(3)}</div>
                    </div>
                    <div className="text-center">
                      <div className="text-slate-400">Theta</div>
                      <div className="text-white font-medium">{product.thetaValue?.toFixed(3)}</div>
                    </div>
                  </div>
                )}

                {/* Product Info */}
                <div className="flex items-center justify-between text-sm text-slate-400 mb-4">
                  <div className="flex items-center">
                    <Activity className="w-4 h-4 mr-1" />
                    v{product.currentVersion}
                  </div>
                  <div className="text-right">
                    <div>by {product.ownerName}</div>
                    <div className="text-xs">
                      {new Date(product.updatedAt).toLocaleDateString()}
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center justify-between pt-4 border-t border-slate-700">
                  <div className="flex space-x-2">
                    <Link
                      to={`/products/${product.id}`}
                      className="p-2 text-slate-400 hover:text-emerald-400 hover:bg-emerald-500/10 rounded-lg transition-all duration-200"
                      title="View Product"
                    >
                      <Eye className="w-4 h-4" />
                    </Link>
                    {product.status === ProductStatus.DRAFT && (
                      <Link
                        to={`/products/${product.id}/edit`}
                        className="p-2 text-slate-400 hover:text-blue-400 hover:bg-blue-500/10 rounded-lg transition-all duration-200"
                        title="Edit Product"
                      >
                        <Edit className="w-4 h-4" />
                      </Link>
                    )}
                    <button
                      onClick={() => handleRepriceProduct(product.id)}
                      className="p-2 text-slate-400 hover:text-amber-400 hover:bg-amber-500/10 rounded-lg transition-all duration-200"
                      title="Reprice Product"
                    >
                      <Activity className="w-4 h-4" />
                    </button>
                    {(product.status === ProductStatus.DRAFT || product.status === ProductStatus.CANCELLED) && (
                      <button
                        onClick={() => handleDeleteProduct(product.id)}
                        className="p-2 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all duration-200"
                        title="Delete Product"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                  
                  {product.status === ProductStatus.DRAFT && (
                    <button
                      onClick={() => handleIssueProduct(product.id)}
                      className="bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-3 py-1 rounded-md text-sm font-medium transition-all duration-200"
                    >
                      Issue Product
                    </button>
                  )}
                  
                  {product.status === ProductStatus.ISSUED && (
                    <div className="bg-blue-500/20 text-blue-400 px-3 py-1 rounded-md text-sm font-medium">
                      Active
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};

export default ProductsPage;