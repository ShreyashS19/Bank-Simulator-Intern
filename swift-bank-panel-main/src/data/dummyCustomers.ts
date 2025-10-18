export interface Customer {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  aadharNumber: string;
  dob: string;
  status: string;
  accountNumber: string;
  address: string;
  customerPin: string;
}

export const dummyCustomers: Customer[] = [
  {
    id: 1,
    name: "Rajesh Kumar",
    email: "rajesh.kumar@email.com",
    phoneNumber: "+91 98765 43210",
    aadharNumber: "1234 5678 9012",
    dob: "1990-05-15",
    status: "active",
    accountNumber: "ACC001234567890",
    address: "123 MG Road, Bangalore, Karnataka",
    customerPin: "1234"
  },
  {
    id: 2,
    name: "Priya Sharma",
    email: "priya.sharma@email.com",
    phoneNumber: "+91 87654 32109",
    aadharNumber: "2345 6789 0123",
    dob: "1985-08-22",
    status: "active",
    accountNumber: "ACC002345678901",
    address: "456 Park Street, Mumbai, Maharashtra",
    customerPin: "5678"
  },
  {
    id: 3,
    name: "Amit Patel",
    email: "amit.patel@email.com",
    phoneNumber: "+91 76543 21098",
    aadharNumber: "3456 7890 1234",
    dob: "1992-12-10",
    status: "inactive",
    accountNumber: "ACC003456789012",
    address: "789 Lake View, Ahmedabad, Gujarat",
    customerPin: "9012"
  },
  {
    id: 4,
    name: "Sneha Reddy",
    email: "sneha.reddy@email.com",
    phoneNumber: "+91 65432 10987",
    aadharNumber: "4567 8901 2345",
    dob: "1988-03-18",
    status: "active",
    accountNumber: "ACC004567890123",
    address: "321 IT Park, Hyderabad, Telangana",
    customerPin: "3456"
  },
  {
    id: 5,
    name: "Vikram Singh",
    email: "vikram.singh@email.com",
    phoneNumber: "+91 54321 09876",
    aadharNumber: "5678 9012 3456",
    dob: "1995-07-25",
    status: "active",
    accountNumber: "ACC005678901234",
    address: "654 Heritage Street, Jaipur, Rajasthan",
    customerPin: "7890"
  }
];
