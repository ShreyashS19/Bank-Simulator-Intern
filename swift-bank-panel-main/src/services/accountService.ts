import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/bank-simulator/api';

export interface Account {
  accountId?: string;
  customerId?: string;
  accountNumber: string;
  aadharNumber: string;
  ifscCode: string;
  phoneNumberLinked: string;
  amount: number;
  bankName: string;
  nameOnAccount: string;
  status: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const accountService = {
  getAllAccounts: async (): Promise<Account[]> => {
    try {
      const response = await axios.get<ApiResponse<Account[]>>(`${API_BASE_URL}/account`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching accounts:', error);
      throw error;
    }
  },

  getAccountByNumber: async (accountNumber: string): Promise<Account> => {
    try {
      const response = await axios.get<ApiResponse<Account>>(`${API_BASE_URL}/account/number/${accountNumber}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching account:', error);
      throw error;
    }
  },

  createAccount: async (account: Omit<Account, 'accountId'>): Promise<string> => {
    try {
      const response = await axios.post<ApiResponse<string>>(`${API_BASE_URL}/account/add`, account);
      return response.data.data;
    } catch (error) {
      console.error('Error creating account:', error);
      throw error;
    }
  },

  updateAccount: async (accountNumber: string, account: Partial<Account>): Promise<void> => {
    try {
      await axios.put(`${API_BASE_URL}/account/number/${accountNumber}`, account);
    } catch (error) {
      console.error('Error updating account:', error);
      throw error;
    }
  },

  deleteAccount: async (accountNumber: string): Promise<void> => {
    try {
      await axios.delete(`${API_BASE_URL}/account/number/${accountNumber}`);
    } catch (error) {
      console.error('Error deleting account:', error);
      throw error;
    }
  }
};
