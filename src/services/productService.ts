import axios, { AxiosResponse } from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface Product {
  id: string;
  name: string;
  description?: string;
  productType: ProductType;
  underlyingAsset: string;
  linkedStrategyId?: string;
  linkedStrategyName?: string;
  
  // Product terms
  notional: number;
  strikePrice?: number;
  barrierLevel?: number;
  payoffRate?: number;
  
  // Dates
  issueDate?: string;
  maturityDate: string;
  settlementDate?: string;
  
  // Configuration
  configJson: string;
  pricingModel: PricingModel;
  
  // Pricing results
  fairValue?: number;
  impliedVolatility?: number;
  
  // Greeks
  deltaValue?: number;
  gammaValue?: number;
  thetaValue?: number;
  vegaValue?: number;
  rhoValue?: number;
  
  // Status and metadata
  status: ProductStatus;
  currentVersion: number;
  ownerName: string;
  
  // Payoff curve
  payoffCurve?: PayoffPoint[];
  
  createdAt: string;
  updatedAt: string;
  issuedAt?: string;
}

export interface ProductRequest {
  name: string;
  description?: string;
  productType: ProductType;
  underlyingAsset: string;
  linkedStrategyId?: string;
  
  // Product terms
  notional?: number;
  strikePrice?: number;
  barrierLevel?: number;
  payoffRate?: number;
  
  // Dates
  issueDate?: string;
  maturityDate: string;
  settlementDate?: string;
  
  // Configuration
  configJson: string;
  pricingModel?: PricingModel;
  status?: ProductStatus;
  
  // Pricing parameters
  simulationRuns?: number;
  riskFreeRate?: number;
  impliedVolatility?: number;
}

interface PayoffPoint {
  spotPrice: number;
  payoffValue: number;
  probability?: number;
}

interface ProductVersion {
  id: string;
  versionNumber: number;
  configJson: string;
  changeDescription?: string;
  createdAt: string;
}

export enum ProductType {
  DIGITAL_OPTION = 'DIGITAL_OPTION',
  BARRIER_OPTION = 'BARRIER_OPTION',
  KNOCK_IN_OPTION = 'KNOCK_IN_OPTION',
  KNOCK_OUT_OPTION = 'KNOCK_OUT_OPTION',
  DUAL_CURRENCY = 'DUAL_CURRENCY',
  STRATEGY_LINKED_NOTE = 'STRATEGY_LINKED_NOTE',
  CUSTOM_PAYOFF = 'CUSTOM_PAYOFF'
}

export enum ProductStatus {
  DRAFT = 'DRAFT',
  ISSUED = 'ISSUED',
  ACTIVE = 'ACTIVE',
  EXPIRED = 'EXPIRED',
  CANCELLED = 'CANCELLED'
}

export enum PricingModel {
  BLACK_SCHOLES = 'BLACK_SCHOLES',
  MONTE_CARLO = 'MONTE_CARLO',
  BINOMIAL_TREE = 'BINOMIAL_TREE',
  CUSTOM = 'CUSTOM'
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

export const productService = {
  async getProducts(): Promise<Product[]> {
    const response: AxiosResponse<ApiResponse<Product[]>> = await apiClient.get('/products');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch products');
  },

  async getProduct(id: string): Promise<Product> {
    const response: AxiosResponse<ApiResponse<Product>> = await apiClient.get(`/products/${id}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch product');
  },

  async createProduct(data: ProductRequest): Promise<Product> {
    const response: AxiosResponse<ApiResponse<Product>> = await apiClient.post('/products', data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to create product');
  },

  async updateProduct(id: string, data: ProductRequest): Promise<Product> {
    const response: AxiosResponse<ApiResponse<Product>> = await apiClient.put(`/products/${id}`, data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to update product');
  },

  async deleteProduct(id: string): Promise<void> {
    const response: AxiosResponse<ApiResponse> = await apiClient.delete(`/products/${id}`);
    if (!response.data.success) {
      throw new Error('Failed to delete product');
    }
  },

  async issueProduct(id: string): Promise<Product> {
    const response: AxiosResponse<ApiResponse<Product>> = await apiClient.post(`/products/${id}/issue`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to issue product');
  },

  async repriceProduct(id: string): Promise<Product> {
    const response: AxiosResponse<ApiResponse<Product>> = await apiClient.post(`/products/${id}/reprice`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to reprice product');
  },

  async getProductVersions(id: string): Promise<ProductVersion[]> {
    const response: AxiosResponse<ApiResponse<ProductVersion[]>> = await apiClient.get(`/products/${id}/versions`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error('Failed to fetch product versions');
  }
};