import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/bank-simulator/api';

export interface Customer {
  customerId?: string;
  name: string;
  phoneNumber: string;
  email: string;
  address: string;
  aadharNumber: string;
  dob: string;
  status: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const customerService = {
  getAllCustomers: async (): Promise<Customer[]> => {
    try {
      const response = await axios.get<ApiResponse<Customer[]>>(`${API_BASE_URL}/customer/all`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching customers:', error);
      throw error;
    }
  },

  getCustomerByAadhar: async (aadharNumber: string): Promise<Customer> => {
    try {
      const response = await axios.get<ApiResponse<Customer>>(`${API_BASE_URL}/customer/aadhar/${aadharNumber}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching customer:', error);
      throw error;
    }
  },

  createCustomer: async (customer: Omit<Customer, 'customerId'>): Promise<string> => {
    try {
      const response = await axios.post<ApiResponse<string>>(`${API_BASE_URL}/customer/onboard`, customer);
      return response.data.data;
    } catch (error) {
      console.error('Error creating customer:', error);
      throw error;
    }
  },

  updateCustomer: async (customerId: string, customer: Partial<Customer>): Promise<void> => {
    try {
      await axios.put(`${API_BASE_URL}/customer/${customerId}`, customer);
    } catch (error) {
      console.error('Error updating customer:', error);
      throw error;
    }
  },

  deleteCustomer: async (aadharNumber: string): Promise<void> => {
    try {
      await axios.delete(`${API_BASE_URL}/customer/aadhar/${aadharNumber}`);
    } catch (error) {
      console.error('Error deleting customer:', error);
      throw error;
    }
  }
};
