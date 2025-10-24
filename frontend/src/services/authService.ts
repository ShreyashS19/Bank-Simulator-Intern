import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/bank-simulator/api';

export interface SignupRequest {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface User {
  id: string;
  fullName: string;
  email: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  data: User;
  timestamp: string;
}

export interface CustomerCheckResponse {
  success: boolean;
  message: string;
  data: {
    hasCustomerRecord: boolean;
    userId: string;
    email: string;
  };
  timestamp: string;
}

export interface UserStatus {
  email: string;
  active: boolean;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const authService = {
  signup: async (request: SignupRequest): Promise<AuthResponse> => {
    const response = await axios.post(`${API_BASE_URL}/auth/signup`, request);
    return response.data;
  },

  login: async (request: LoginRequest): Promise<AuthResponse> => {
    const response = await axios.post(`${API_BASE_URL}/auth/login`, request);
    return response.data;
  },

  checkCustomerExists: async (email: string): Promise<CustomerCheckResponse> => {
    const response = await axios.get<CustomerCheckResponse>(
      `${API_BASE_URL}/auth/check-customer?email=${encodeURIComponent(email)}`
    );
    return response.data;
  },

  getAllUsers: async (): Promise<ApiResponse<User[]>> => {
    const response = await axios.get(`${API_BASE_URL}/auth/users/all`);
    return response.data;
  },

  updateUserStatus: async (email: string, active: boolean): Promise<ApiResponse> => {
    const response = await axios.put(
      `${API_BASE_URL}/auth/user/status?email=${encodeURIComponent(email)}&active=${active}`
    );
    return response.data;
  },

  getUserByEmail: async (email: string): Promise<ApiResponse<User>> => {
    const response = await axios.get(
      `${API_BASE_URL}/auth/user?email=${encodeURIComponent(email)}`
    );
    return response.data;
  },
};

export default authService;
