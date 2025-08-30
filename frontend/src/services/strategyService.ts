import axios, { AxiosResponse } from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface Strategy {
  id: string;
  name: string;
  description?: string;
  symbol: string;
  timeframe: string;
  configJson: string;
  status: StrategyStatus;
  tags?: string[];
  currentVersion: number;
  ownerName: string;
  createdAt: string;
  updatedAt: string;
}

export interface StrategyRequest {
  name: string;
  description?: string;
  symbol: string;
  timeframe?: string;
  configJson: string;
  status?: StrategyStatus;
  tags?: string[];
}

export interface SignalEvaluationRequest {
  symbol: string;
  configJson: string;
  timeframe?: string;
}

export interface SignalEvaluationResponse {
  signal: SignalType;
  currentPrice: number;
  indicatorValues: Record<string, any>;
  matchedRules: string[];
  confidenceScore: number;
  evaluatedAt: string;
  message: string;
}

interface StrategyVersion {
  id: string;
  versionNumber: number;
  configJson: string;
  changeDescription?: string;
  createdAt: string;
}

export enum StrategyStatus {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  PAUSED = 'PAUSED',
  ARCHIVED = 'ARCHIVED'
}

export enum SignalType {
  BUY = 'BUY',
  SELL = 'SELL',
  HOLD = 'HOLD',
  NO_SIGNAL = 'NO_SIGNAL'
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

export const strategyService = {
  async getStrategies(): Promise<Strategy[]> {
    const response: AxiosResponse<ApiResponse<Strategy[]>> = await apiClient.get('/strategies');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch strategies');
  },

  async getStrategy(id: string): Promise<Strategy> {
    const response: AxiosResponse<ApiResponse<Strategy>> = await apiClient.get(`/strategies/${id}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch strategy');
  },

  async createStrategy(data: StrategyRequest): Promise<Strategy> {
    const response: AxiosResponse<ApiResponse<Strategy>> = await apiClient.post('/strategies', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to create strategy');
  },

  async updateStrategy(id: string, data: StrategyRequest): Promise<Strategy> {
    const response: AxiosResponse<ApiResponse<Strategy>> = await apiClient.put(`/strategies/${id}`, data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to update strategy');
  },

  async deleteStrategy(id: string): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await apiClient.delete(`/strategies/${id}`);
    if (!response.data.success) {
      throw new Error('Failed to delete strategy');
    }
  },

  async evaluateStrategy(data: SignalEvaluationRequest): Promise<SignalEvaluationResponse> {
    const response: AxiosResponse<ApiResponse<SignalEvaluationResponse>> = await apiClient.post('/strategies/evaluate', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to evaluate strategy');
  },

  async getStrategyVersions(id: string): Promise<StrategyVersion[]> {
    const response: AxiosResponse<ApiResponse<StrategyVersion[]>> = await apiClient.get(`/strategies/${id}/versions`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch strategy versions');
  }
};