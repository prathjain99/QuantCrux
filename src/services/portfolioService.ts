import axios, { AxiosResponse } from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface Portfolio {
  id: string;
  name: string;
  description?: string;
  ownerName: string;
  managerName?: string;
  ownerId: string;
  managerId?: string;
  
  initialCapital: number;
  currentNav: number;
  cashBalance: number;
  totalPnl: number;
  totalReturnPct: number;
  
  // Risk metrics
  var95?: number;
  volatility?: number;
  beta?: number;
  maxDrawdown?: number;
  sharpeRatio?: number;
  
  // Status and settings
  status: PortfolioStatus;
  currency: string;
  benchmarkSymbol: string;
  
  // Holdings and history
  holdings?: Holding[];
  navHistory?: NAVPoint[];
  recentTransactions?: Transaction[];
  
  // Allocation breakdowns
  assetAllocation?: AllocationBreakdown[];
  sectorAllocation?: AllocationBreakdown[];
  
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioRequest {
  name: string;
  description?: string;
  initialCapital: number;
  managerId?: string;
  status?: PortfolioStatus;
  currency?: string;
  benchmarkSymbol?: string;
}

export interface Holding {
  id: string;
  instrumentType: InstrumentType;
  symbol: string;
  quantity: number;
  avgPrice: number;
  latestPrice?: number;
  marketValue?: number;
  costBasis?: number;
  unrealizedPnl?: number;
  realizedPnl?: number;
  sector?: string;
  assetClass?: string;
  weightPct?: number;
}

export interface NAVPoint {
  date: string;
  nav: number;
  dailyReturn?: number;
}

export interface Transaction {
  id: string;
  transactionType: TransactionType;
  symbol?: string;
  quantity?: number;
  price?: number;
  amount: number;
  description?: string;
  executedAt: string;
}

export interface AllocationBreakdown {
  category: string;
  value: number;
  percentage: number;
  pnl?: number;
}

export enum PortfolioStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  ARCHIVED = 'ARCHIVED'
}

export enum InstrumentType {
  ASSET = 'ASSET',
  STRATEGY = 'STRATEGY',
  PRODUCT = 'PRODUCT'
}

export enum TransactionType {
  BUY = 'BUY',
  SELL = 'SELL',
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  DIVIDEND = 'DIVIDEND',
  INTEREST = 'INTEREST',
  STRATEGY_ALLOCATION = 'STRATEGY_ALLOCATION',
  PRODUCT_PURCHASE = 'PRODUCT_PURCHASE',
  FEE = 'FEE'
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

export const portfolioService = {
  async getPortfolios(): Promise<Portfolio[]> {
    const response: AxiosResponse<ApiResponse<Portfolio[]>> = await apiClient.get('/portfolios');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch portfolios');
  },

  async getPortfolio(id: string): Promise<Portfolio> {
    const response: AxiosResponse<ApiResponse<Portfolio>> = await apiClient.get(`/portfolios/${id}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch portfolio');
  },

  async createPortfolio(data: PortfolioRequest): Promise<Portfolio> {
    const response: AxiosResponse<ApiResponse<Portfolio>> = await apiClient.post('/portfolios', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to create portfolio');
  },

  async updatePortfolio(id: string, data: PortfolioRequest): Promise<Portfolio> {
    const response: AxiosResponse<ApiResponse<Portfolio>> = await apiClient.put(`/portfolios/${id}`, data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to update portfolio');
  },

  async deletePortfolio(id: string): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await apiClient.delete(`/portfolios/${id}`);
    if (!response.data.success) {
      throw new Error('Failed to delete portfolio');
    }
  },

  async refreshPortfolioMetrics(id: string): Promise<Portfolio> {
    const response: AxiosResponse<ApiResponse<Portfolio>> = await apiClient.post(`/portfolios/${id}/refresh`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to refresh portfolio metrics');
  },

  async getPortfolioNAVHistory(id: string): Promise<NAVPoint[]> {
    const response: AxiosResponse<ApiResponse<NAVPoint[]>> = await apiClient.get(`/portfolios/${id}/nav`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch NAV history');
  }
};