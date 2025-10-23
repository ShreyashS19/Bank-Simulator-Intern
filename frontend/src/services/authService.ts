import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/bank-simulator/api';

export interface SignupRequest {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
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

export interface LoginRequest {
  email: string;
  password: string;
}

export interface User {
  id: string;
  fullName: string;
  email: string;
  createdAt?: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  data: User;
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
  }
};



