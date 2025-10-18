import { Customer, dummyCustomers } from "@/data/dummyCustomers";
import { Account, dummyAccounts } from "@/data/dummyAccounts";

// Simulate network delay
const delay = (ms: number = 500) => new Promise(resolve => setTimeout(resolve, ms));

// Customer API
let customers = [...dummyCustomers];

export const customerApi = {
  getAll: async (): Promise<Customer[]> => {
    await delay();
    return [...customers];
  },

 

  getByAadharNumber: async (aadharNumber: string): Promise<Customer | null> => {
    await delay();
    return customers.find(c => c.aadharNumber === aadharNumber) || null;
  },

  create: async (customer: Omit<Customer, 'id'>): Promise<Customer> => {
    await delay();
    const newCustomer = { ...customer, id: Math.max(...customers.map(c => c.id)) + 1 };
    customers.push(newCustomer);
    return newCustomer;
  },

  update: async (id: number, updates: Partial<Customer>): Promise<Customer> => {
    await delay();
    const index = customers.findIndex(c => c.id === id);
    if (index === -1) throw new Error("Customer not found");
    customers[index] = { ...customers[index], ...updates };
    return customers[index];
  },

  delete: async (id: number): Promise<void> => {
    await delay();
    customers = customers.filter(c => c.id !== id);
  }
};

// Account API
let accounts = [...dummyAccounts];

export const accountApi = {
  getAll: async (): Promise<Account[]> => {
    await delay();
    return [...accounts];
  },

  getByAccountNumber: async (accountNumber: string): Promise<Account | null> => {
    await delay();
    return accounts.find(a => a.accountNumber === accountNumber) || null;
  },

  create: async (account: Omit<Account, 'id'>): Promise<Account> => {
    await delay();
    const newAccount = { ...account, id: Math.max(...accounts.map(a => a.id)) + 1 };
    accounts.push(newAccount);
    return newAccount;
  },

  update: async (id: number, updates: Partial<Account>): Promise<Account> => {
    await delay();
    const index = accounts.findIndex(a => a.id === id);
    if (index === -1) throw new Error("Account not found");
    accounts[index] = { ...accounts[index], ...updates };
    return accounts[index];
  },

  delete: async (id: number): Promise<void> => {
    await delay();
    accounts = accounts.filter(a => a.id !== id);
  }
};
