import axios, { AxiosResponse } from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface MarketData {
  symbol: string;
  dataType: DataType;
  timeframe?: string;
  
  // Live price data
  price?: number;
  bidPrice?: number;
  askPrice?: number;
  dayChange?: number;
  dayChangePercent?: number;
  volume?: number;
  
  // OHLCV data
  ohlcvData?: OHLCVData[];
  
  // Metadata
  dataTimestamp: string;
  source: string;
  qualityScore: number;
  isStale?: boolean;
  message?: string;
}

export interface OHLCVData {
  timestamp: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface MarketDataRequest {
  symbol: string;
  dataType?: DataType;
  timeframe?: string;
  startTime?: string;
  endTime?: string;
  limit?: number;
  preferredSource?: string;
  forceRefresh?: boolean;
}

export interface SymbolSearch {
  symbol: string;
  name: string;
  exchange?: string;
  currency?: string;
  assetType: AssetType;
  sector?: string;
  industry?: string;
  country?: string;
  isTradeable?: boolean;
  marketCap?: number;
  description?: string;
}

export enum DataType {
  LIVE_PRICE = 'LIVE_PRICE',
  OHLCV = 'OHLCV',
  INTRADAY = 'INTRADAY'
}

export enum AssetType {
  STOCK = 'STOCK',
  CRYPTO = 'CRYPTO',
  ETF = 'ETF',
  FOREX = 'FOREX',
  INDEX = 'INDEX',
  COMMODITY = 'COMMODITY'
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

export const marketDataService = {
  async getLivePrice(symbol: string, forceRefresh: boolean = false): Promise<MarketData> {
    const response: AxiosResponse<ApiResponse<MarketData>> = await apiClient.get(
      `/market-data/price/${symbol}`, 
      { params: { forceRefresh } }
    );
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch live price');
  },

  async getOHLCVData(symbol: string, timeframe: string = '1d', startTime: string, endTime: string, limit: number = 100): Promise<MarketData> {
    const response: AxiosResponse<ApiResponse<MarketData>> = await apiClient.get(
      `/market-data/ohlcv/${symbol}`, 
      { 
        params: { 
          timeframe, 
          startTime, 
          endTime, 
          limit 
        } 
      }
    );
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch OHLCV data');
  },

  async getBatchMarketData(requests: MarketDataRequest[]): Promise<MarketData[]> {
    const response: AxiosResponse<ApiResponse<MarketData[]>> = await apiClient.post('/market-data/batch', requests);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch batch market data');
  },

  async searchSymbols(query: string): Promise<SymbolSearch[]> {
    const response: AxiosResponse<ApiResponse<SymbolSearch[]>> = await apiClient.get('/market-data/search', {
      params: { query }
    });
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to search symbols');
  },

  async getPopularSymbols(assetType?: AssetType): Promise<SymbolSearch[]> {
    const response: AxiosResponse<ApiResponse<SymbolSearch[]>> = await apiClient.get('/market-data/popular', {
      params: assetType ? { assetType } : {}
    });
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch popular symbols');
  },

  async getBenchmarkData(symbol: string, startTime: string, endTime: string): Promise<MarketData[]> {
    const response: AxiosResponse<ApiResponse<MarketData[]>> = await apiClient.get(
      `/market-data/benchmark/${symbol}`, 
      { 
        params: { 
          startTime, 
          endTime 
        } 
      }
    );
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch benchmark data');
  },

  async refreshCache(): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await apiClient.post('/market-data/refresh-cache');
    if (!response.data.success) {
      throw new Error('Failed to refresh cache');
    }
  },

  // Utility methods
  formatPrice(price?: number): string {
    if (price === undefined || price === null) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 6,
    }).format(price);
  },

  formatPercentage(value?: number): string {
    if (value === undefined || value === null) return 'N/A';
    const percentage = value * 100;
    return `${percentage >= 0 ? '+' : ''}${percentage.toFixed(2)}%`;
  },

  formatVolume(volume?: number): string {
    if (volume === undefined || volume === null) return 'N/A';
    
    if (volume >= 1000000000) {
      return `${(volume / 1000000000).toFixed(1)}B`;
    } else if (volume >= 1000000) {
      return `${(volume / 1000000).toFixed(1)}M`;
    } else if (volume >= 1000) {
      return `${(volume / 1000).toFixed(1)}K`;
    }
    
    return volume.toLocaleString();
  },

  getAssetTypeColor(assetType: AssetType): string {
    switch (assetType) {
      case AssetType.STOCK:
        return 'text-blue-400 bg-blue-500/10 border-blue-500/20';
      case AssetType.CRYPTO:
        return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
      case AssetType.ETF:
        return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
      case AssetType.FOREX:
        return 'text-purple-400 bg-purple-500/10 border-purple-500/20';
      case AssetType.INDEX:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
      case AssetType.COMMODITY:
        return 'text-red-400 bg-red-500/10 border-red-500/20';
      default:
        return 'text-slate-400 bg-slate-500/10 border-slate-500/20';
    }
  },

  getTrendColor(dayChangePercent?: number): string {
    if (dayChangePercent === undefined || dayChangePercent === null) return 'text-slate-400';
    return dayChangePercent >= 0 ? 'text-emerald-400' : 'text-red-400';
  }
};