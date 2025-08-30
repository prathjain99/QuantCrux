import axios, { AxiosResponse } from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface Backtest {
  id: string;
  name: string;
  strategyName: string;
  strategyId: string;
  symbol: string;
  timeframe: string;
  startDate: string;
  endDate: string;
  initialCapital: number;
  status: BacktestStatus;
  progress: number;
  errorMessage?: string;
  
  // Results
  finalCapital?: number;
  totalReturn?: number;
  totalTrades?: number;
  winningTrades?: number;
  losingTrades?: number;
  
  // Metrics
  sharpeRatio?: number;
  sortinoRatio?: number;
  maxDrawdown?: number;
  maxDrawdownDuration?: number;
  cagr?: number;
  volatility?: number;
  profitFactor?: number;
  winRate?: number;
  avgTradeDuration?: number;
  
  // Charts
  equityCurve?: EquityPoint[];
  drawdownCurve?: DrawdownPoint[];
  monthlyReturns?: any;
  
  ownerName: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
}

export interface BacktestRequest {
  strategyId: string;
  strategyVersionId?: string;
  name: string;
  symbol: string;
  timeframe?: string;
  startDate: string;
  endDate: string;
  initialCapital?: number;
  commissionRate?: number;
  slippageRate?: number;
  monteCarloEnabled?: boolean;
  monteCarloRuns?: number;
  walkForwardEnabled?: boolean;
  benchmarkSymbol?: string;
}

interface EquityPoint {
  timestamp: string;
  equity: number;
}

interface DrawdownPoint {
  timestamp: string;
  drawdown: number;
}

export enum BacktestStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
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

export const backtestService = {
  async getBacktests(): Promise<Backtest[]> {
    const response: AxiosResponse<ApiResponse<Backtest[]>> = await apiClient.get('/backtests');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch backtests');
  },

  async getBacktest(id: string): Promise<Backtest> {
    const response: AxiosResponse<ApiResponse<Backtest>> = await apiClient.get(`/backtests/${id}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch backtest');
  },

  async createBacktest(data: BacktestRequest): Promise<Backtest> {
    const response: AxiosResponse<ApiResponse<Backtest>> = await apiClient.post('/backtests', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to create backtest');
  },

  async deleteBacktest(id: string): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await apiClient.delete(`/backtests/${id}`);
    if (!response.data.success) {
      throw new Error('Failed to delete backtest');
    }
  },

  async getStrategyBacktests(strategyId: string): Promise<Backtest[]> {
    const response: AxiosResponse<ApiResponse<Backtest[]>> = await apiClient.get(`/backtests/strategy/${strategyId}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch strategy backtests');
  }
};