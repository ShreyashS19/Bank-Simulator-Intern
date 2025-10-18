export interface Account {
  id: number;
  accountNumber: string;
  customerName: string;
  accountType: "Savings" | "Current";
  balance: number;
  status: string;
  aadharNumber: string;
  ifscCode: string;
  bankName: string;
  nameOnAccount: string;
}

export const dummyAccounts: Account[] = [
  {
    id: 1,
    accountNumber: "ACC001234567890",
    customerName: "Rajesh Kumar",
    accountType: "Savings",
    balance: 125000.50,
    status: "active",
    aadharNumber: "1234 5678 9012",
    ifscCode: "SBIN0001234",
    bankName: "State Bank of India",
    nameOnAccount: "Rajesh Kumar"
  },
  {
    id: 2,
    accountNumber: "ACC002345678901",
    customerName: "Priya Sharma",
    accountType: "Current",
    balance: 450000.75,
    status: "active",
    aadharNumber: "2345 6789 0123",
    ifscCode: "HDFC0002345",
    bankName: "HDFC Bank",
    nameOnAccount: "Priya Sharma"
  },
  {
    id: 3,
    accountNumber: "ACC003456789012",
    customerName: "Amit Patel",
    accountType: "Savings",
    balance: 75000.00,
    status: "inactive",
    aadharNumber: "3456 7890 1234",
    ifscCode: "ICIC0003456",
    bankName: "ICICI Bank",
    nameOnAccount: "Amit Patel"
  },
  {
    id: 4,
    accountNumber: "ACC004567890123",
    customerName: "Sneha Reddy",
    accountType: "Savings",
    balance: 320000.25,
    status: "active",
    aadharNumber: "4567 8901 2345",
    ifscCode: "AXIS0004567",
    bankName: "Axis Bank",
    nameOnAccount: "Sneha Reddy"
  },
  {
    id: 5,
    accountNumber: "ACC005678901234",
    customerName: "Vikram Singh",
    accountType: "Current",
    balance: 890000.00,
    status: "active",
    aadharNumber: "5678 9012 3456",
    ifscCode: "PUNB0005678",
    bankName: "Punjab National Bank",
    nameOnAccount: "Vikram Singh"
  }
];
