import axios, { AxiosResponse } from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface Order {
  id: string;
  portfolioId: string;
  portfolioName: string;
  instrumentId?: string;
  instrumentType: InstrumentType;
  symbol: string;
  
  // Order details
  side: OrderSide;
  orderType: OrderType;
  quantity: number;
  limitPrice?: number;
  stopPrice?: number;
  
  // Execution details
  filledQuantity: number;
  avgFillPrice?: number;
  totalFees: number;
  
  // Status and lifecycle
  status: OrderStatus;
  timeInForce: TimeInForce;
  
  // Timestamps
  createdAt: string;
  submittedAt?: string;
  executedAt?: string;
  cancelledAt?: string;
  expiresAt?: string;
  
  // Metadata
  notes?: string;
  clientOrderId?: string;
  userName: string;
  
  // Calculated fields
  remainingQuantity: number;
  fillPercentage: number;
  estimatedValue?: number;
}

export interface OrderRequest {
  portfolioId: string;
  instrumentId?: string;
  instrumentType: InstrumentType;
  symbol: string;
  side: OrderSide;
  orderType: OrderType;
  quantity: number;
  limitPrice?: number;
  stopPrice?: number;
  timeInForce?: TimeInForce;
  expiresAt?: string;
  notes?: string;
  clientOrderId?: string;
}

export interface Trade {
  id: string;
  orderId: string;
  portfolioId: string;
  portfolioName: string;
  instrumentId?: string;
  instrumentType: InstrumentType;
  symbol: string;
  
  // Trade details
  side: OrderSide;
  quantity: number;
  price: number;
  totalAmount: number;
  fees: number;
  
  // Execution quality
  expectedPrice?: number;
  slippage?: number;
  executionVenue: string;
  
  // Status and dates
  status: TradeStatus;
  tradeDate: string;
  settlementDate?: string;
  
  createdAt: string;
  executedAt: string;
  settledAt?: string;
  
  // References
  strategyId?: string;
  strategyName?: string;
  productId?: string;
  productName?: string;
  
  // Metadata
  notes?: string;
  executionId?: string;
  userName: string;
  
  // Calculated fields
  netAmount: number;
  executionQuality?: string;
}

export interface Position {
  id: string;
  portfolioId: string;
  portfolioName: string;
  instrumentId?: string;
  instrumentType: InstrumentType;
  symbol: string;
  
  // Position details
  netQuantity: number;
  avgPrice: number;
  costBasis: number;
  marketValue: number;
  unrealizedPnl: number;
  realizedPnl: number;
  
  // Current market data
  currentPrice?: number;
  dayChange?: number;
  dayChangePercent?: number;
  
  // Risk metrics
  delta?: number;
  gamma?: number;
  theta?: number;
  vega?: number;
  
  // Metadata
  firstTradeDate?: string;
  lastTradeDate?: string;
  totalTrades: number;
  
  createdAt: string;
  updatedAt: string;
  
  // Calculated fields
  weightPercent?: number;
  positionType: string; // "Long", "Short", "Flat"
  returnPercent?: number;
}

export interface MarketQuote {
  symbol: string;
  instrumentType: InstrumentType;
  
  // Price data
  bidPrice?: number;
  askPrice?: number;
  lastPrice: number;
  volume?: number;
  
  // Market data
  openPrice?: number;
  highPrice?: number;
  lowPrice?: number;
  prevClose?: number;
  
  // Calculated fields
  dayChange?: number;
  dayChangePercent?: number;
  spread?: number;
  trend?: string; // "UP", "DOWN", "FLAT"
  
  quoteTime: string;
  updatedAt: string;
}

export enum InstrumentType {
  ASSET = 'ASSET',
  STRATEGY = 'STRATEGY',
  PRODUCT = 'PRODUCT'
}

export enum OrderSide {
  BUY = 'BUY',
  SELL = 'SELL'
}

export enum OrderType {
  MARKET = 'MARKET',
  LIMIT = 'LIMIT',
  STOP = 'STOP',
  STOP_LIMIT = 'STOP_LIMIT',
  CONDITIONAL = 'CONDITIONAL'
}

export enum OrderStatus {
  PENDING = 'PENDING',
  SUBMITTED = 'SUBMITTED',
  PARTIALLY_FILLED = 'PARTIALLY_FILLED',
  FILLED = 'FILLED',
  CANCELLED = 'CANCELLED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED'
}

export enum TimeInForce {
  DAY = 'DAY',
  GTC = 'GTC',
  IOC = 'IOC',
  FOK = 'FOK'
}

export enum TradeStatus {
  EXECUTED = 'EXECUTED',
  SETTLED = 'SETTLED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

// Configure axios
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const tradeService = {
  async getOrders(): Promise<Order[]> {
    const response: AxiosResponse<ApiResponse<Order[]>> = await apiClient.get('/trades/orders');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch orders');
  },

  async getTrades(): Promise<Trade[]> {
    const response: AxiosResponse<ApiResponse<Trade[]>> = await apiClient.get('/trades');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch trades');
  },

  async getPositions(): Promise<Position[]> {
    const response: AxiosResponse<ApiResponse<Position[]>> = await apiClient.get('/trades/positions');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch positions');
  },

  async createOrder(data: OrderRequest): Promise<Order> {
    const response: AxiosResponse<ApiResponse<Order>> = await apiClient.post('/trades/orders', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to create order');
  },

  async cancelOrder(id: string): Promise<Order> {
    const response: AxiosResponse<ApiResponse<Order>> = await apiClient.put(`/trades/orders/${id}/cancel`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to cancel order');
  },

  async getMarketQuotes(symbols: string[]): Promise<MarketQuote[]> {
    const response: AxiosResponse<ApiResponse<MarketQuote[]>> = await apiClient.get('/trades/quotes', {
      params: { symbols: symbols.join(',') }
    });
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch market quotes');
  },

  async getMarketQuote(symbol: string, instrumentType: InstrumentType = InstrumentType.ASSET): Promise<MarketQuote> {
    const response: AxiosResponse<ApiResponse<MarketQuote>> = await apiClient.get(`/trades/quotes/${symbol}`, {
      params: { instrumentType }
    });
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch market quote');
  }
};