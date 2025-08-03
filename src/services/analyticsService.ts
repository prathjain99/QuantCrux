import axios, { AxiosResponse } from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface AnalyticsData {
  portfolioId?: string;
  strategyId?: string;
  portfolioName?: string;
  strategyName?: string;
  periodStart: string;
  periodEnd: string;
  benchmarkSymbol: string;
  
  // Risk metrics
  var95?: number;
  var99?: number;
  volatility?: number;
  beta?: number;
  alpha?: number;
  sharpeRatio?: number;
  sortinoRatio?: number;
  maxDrawdown?: number;
  maxDrawdownDuration?: number;
  calmarRatio?: number;
  informationRatio?: number;
  correlationToBenchmark?: number;
  trackingError?: number;
  
  // Performance metrics
  totalReturn?: number;
  cagr?: number;
  annualizedReturn?: number;
  excessReturn?: number;
  totalTrades?: number;
  winningTrades?: number;
  losingTrades?: number;
  winRate?: number;
  avgWin?: number;
  avgLoss?: number;
  profitFactor?: number;
  tradeFrequency?: number;
  avgHoldingPeriod?: number;
  turnoverRatio?: number;
  benchmarkReturn?: number;
  outperformance?: number;
  
  // Attribution data
  assetAttribution?: Record<string, number>;
  sectorAttribution?: Record<string, number>;
  strategyAttribution?: Record<string, number>;
  productAttribution?: Record<string, number>;
  
  // Correlation data
  correlationMatrix?: Record<string, Record<string, number>>;
  avgCorrelation?: number;
  maxCorrelation?: number;
  minCorrelation?: number;
  diversificationRatio?: number;
}

export interface AnalyticsRequest {
  portfolioId?: string;
  strategyId?: string;
  periodStart: string;
  periodEnd: string;
  benchmarkSymbol?: string;
  includeCorrelations?: boolean;
  includeAttribution?: boolean;
}

export interface Report {
  id: string;
  portfolioId?: string;
  strategyId?: string;
  portfolioName?: string;
  strategyName?: string;
  reportType: ReportType;
  reportName: string;
  description?: string;
  periodStart?: string;
  periodEnd?: string;
  fileFormat: FileFormat;
  filePath?: string;
  fileSize?: number;
  status: ReportStatus;
  errorMessage?: string;
  userName: string;
  
  createdAt: string;
  generatedAt?: string;
  downloadedAt?: string;
  expiresAt?: string;
  
  // Calculated fields
  downloadUrl?: string;
  isExpired?: boolean;
  fileSizeFormatted?: string;
}

export interface ReportRequest {
  portfolioId?: string;
  strategyId?: string;
  reportType: ReportType;
  reportName: string;
  description?: string;
  periodStart?: string;
  periodEnd?: string;
  fileFormat: FileFormat;
  templateConfig?: string;
  filters?: string;
  
  // Report options
  benchmarkSymbol?: string;
  includeCharts?: boolean;
  includeCorrelations?: boolean;
  includeAttribution?: boolean;
  includeBenchmarkComparison?: boolean;
}

export enum ReportType {
  PORTFOLIO_SUMMARY = 'PORTFOLIO_SUMMARY',
  RISK_ANALYSIS = 'RISK_ANALYSIS',
  PERFORMANCE_REPORT = 'PERFORMANCE_REPORT',
  TRADE_BLOTTER = 'TRADE_BLOTTER',
  ATTRIBUTION_ANALYSIS = 'ATTRIBUTION_ANALYSIS',
  CORRELATION_REPORT = 'CORRELATION_REPORT',
  CUSTOM = 'CUSTOM'
}

export enum FileFormat {
  PDF = 'PDF',
  CSV = 'CSV',
  XLS = 'XLS'
}

export enum ReportStatus {
  PENDING = 'PENDING',
  GENERATING = 'GENERATING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  EXPIRED = 'EXPIRED'
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

export const analyticsService = {
  async getPortfolioAnalytics(portfolioId: string, request: AnalyticsRequest): Promise<AnalyticsData> {
    const response: AxiosResponse<ApiResponse<AnalyticsData>> = await apiClient.post(`/analytics/portfolio/${portfolioId}`, request);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch portfolio analytics');
  },

  async getStrategyAnalytics(strategyId: string, request: AnalyticsRequest): Promise<AnalyticsData> {
    const response: AxiosResponse<ApiResponse<AnalyticsData>> = await apiClient.post(`/analytics/strategy/${strategyId}`, request);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch strategy analytics');
  },

  async generateReport(request: ReportRequest): Promise<Report> {
    const response: AxiosResponse<ApiResponse<Report>> = await apiClient.post('/analytics/reports', request);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to generate report');
  },

  async getReports(): Promise<Report[]> {
    const response: AxiosResponse<ApiResponse<Report[]>> = await apiClient.get('/analytics/reports');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch reports');
  },

  async getReport(id: string): Promise<Report> {
    const response: AxiosResponse<ApiResponse<Report>> = await apiClient.get(`/analytics/reports/${id}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch report');
  },

  async deleteReport(id: string): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await apiClient.delete(`/analytics/reports/${id}`);
    if (!response.data.success) {
      throw new Error('Failed to delete report');
    }
  },

  async downloadReport(id: string): Promise<Blob> {
    const response = await apiClient.get(`/analytics/reports/${id}/download`, {
      responseType: 'blob'
    });
    return response.data;
  }
};