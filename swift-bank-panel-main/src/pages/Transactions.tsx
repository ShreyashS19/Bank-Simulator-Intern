import { useState } from "react";
import { motion } from "framer-motion";
import DashboardLayout from "@/components/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";
import { Search, ArrowLeftRight, CheckCircle, TrendingUp, Download, Loader2 } from "lucide-react";
import { transactionService, Transaction } from "@/services/transactionService";
import { exportTransactionsToExcel } from "@/utils/excelExport";

const Transactions = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [searchedTransactions, setSearchedTransactions] = useState<Transaction[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchAccountNumber, setSearchAccountNumber] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [formData, setFormData] = useState({
    senderAccountNumber: "",
    receiverAccountNumber: "",
    amount: "",
    transactionType: "transfer",
    description: "",
    pin: ""
  });

  const handleSearchByAccount = async () => {
    if (!searchAccountNumber.trim()) {
      toast.error("Please enter an account number");
      return;
    }

    if (!/^\d+$/.test(searchAccountNumber)) {
      toast.error("Account number must contain only digits");
      return;
    }

    setIsLoading(true);
    setHasSearched(true);
    try {
      const data = await transactionService.getTransactionsByAccount(searchAccountNumber);
      setSearchedTransactions(data);
      if (data.length === 0) {
        toast.info(`No transactions found for account ${searchAccountNumber}`);
      } else {
        toast.success(`Found ${data.length} transaction(s)`);
      }
    } catch (error) {
      toast.error("Failed to fetch transactions. Please try again.");
      setSearchedTransactions([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownloadExcel = () => {
    if (searchedTransactions.length === 0) {
      toast.error("No transactions to download");
      return;
    }
    exportTransactionsToExcel(searchedTransactions, searchAccountNumber);
    toast.success("Excel file downloaded successfully!");
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const newTransaction: Transaction = {
      id: Date.now(),
      ...formData,
      status: "success",
      timestamp: new Date().toISOString()
    };
    setTransactions([newTransaction, ...transactions]);
    toast.success("Transaction completed successfully!");
    handleReset();
  };

  const handleReset = () => {
    setFormData({
      senderAccountNumber: "",
      receiverAccountNumber: "",
      amount: "",
      transactionType: "transfer",
      description: "",
      pin: ""
    });
  };

  const filteredTransactions = transactions.filter(transaction =>
    transaction.senderAccountNumber.includes(searchTerm) ||
    transaction.receiverAccountNumber.includes(searchTerm) ||
    transaction.transactionType.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalTransactions = transactions.length;
  const successfulTransactions = transactions.filter(t => t.status === 'success').length;
  const totalVolume = transactions.reduce((sum, t) => sum + Number(t.amount), 0);

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Transaction Management</h1>
          <p className="text-muted-foreground mt-1">Create and track transactions</p>
        </div>

        {/* Summary Cards */}
        <div className="grid gap-6 md:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Transactions</CardTitle>
              <ArrowLeftRight className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{totalTransactions}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Successful</CardTitle>
              <CheckCircle className="h-4 w-4 text-secondary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{successfulTransactions}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">Total Volume</CardTitle>
              <TrendingUp className="h-4 w-4 text-primary" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">₹{totalVolume.toLocaleString()}</div>
            </CardContent>
          </Card>
        </div>

        {/* Create Transaction Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <ArrowLeftRight className="h-5 w-5" />
              Create New Transaction
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="senderAccountNumber">Sender Account Number</Label>
                  <Input
                    id="senderAccountNumber"
                    value={formData.senderAccountNumber}
                    onChange={(e) => setFormData({ ...formData, senderAccountNumber: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="receiverAccountNumber">Receiver Account Number</Label>
                  <Input
                    id="receiverAccountNumber"
                    value={formData.receiverAccountNumber}
                    onChange={(e) => setFormData({ ...formData, receiverAccountNumber: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="amount">Amount</Label>
                  <Input
                    id="amount"
                    type="number"
                    value={formData.amount}
                    onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="transactionType">Transaction Type</Label>
                  <Select 
                    value={formData.transactionType} 
                    onValueChange={(value) => setFormData({ ...formData, transactionType: value })}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="transfer">Transfer</SelectItem>
                      <SelectItem value="deposit">Deposit</SelectItem>
                      <SelectItem value="withdrawal">Withdrawal</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Input
                    id="description"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="pin">PIN</Label>
                  <Input
                    id="pin"
                    type="password"
                    value={formData.pin}
                    onChange={(e) => setFormData({ ...formData, pin: e.target.value })}
                    required
                  />
                </div>
              </div>
              <div className="flex gap-3">
                <Button type="submit">Create Transaction</Button>
                <Button type="button" variant="outline" onClick={handleReset}>Reset</Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Search Transactions by Account */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Search className="h-5 w-5" />
              Search Transactions by Account Number
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col sm:flex-row gap-4">
              <div className="flex-1">
                <Input
                  placeholder="Enter account number..."
                  value={searchAccountNumber}
                  onChange={(e) => setSearchAccountNumber(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearchByAccount()}
                  disabled={isLoading}
                />
              </div>
              <Button 
                onClick={handleSearchByAccount} 
                disabled={isLoading}
                className="sm:w-auto"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Searching...
                  </>
                ) : (
                  <>
                    <Search className="mr-2 h-4 w-4" />
                    Search
                  </>
                )}
              </Button>
              <Button 
                onClick={handleDownloadExcel}
                variant="secondary"
                disabled={searchedTransactions.length === 0}
                className="sm:w-auto"
              >
                <Download className="mr-2 h-4 w-4" />
                Download Excel
              </Button>
            </div>

            {/* Search Results Table */}
            {hasSearched && (
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="mt-6"
              >
                {isLoading ? (
                  <div className="flex items-center justify-center py-12">
                    <Loader2 className="h-8 w-8 animate-spin text-primary" />
                  </div>
                ) : searchedTransactions.length === 0 ? (
                  <div className="text-center py-12 text-muted-foreground">
                    No transactions found for account number: {searchAccountNumber}
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="border-b">
                          <th className="text-left py-3 px-4 font-medium">Sender Account</th>
                          <th className="text-left py-3 px-4 font-medium">Receiver Account</th>
                          <th className="text-left py-3 px-4 font-medium">Amount</th>
                          <th className="text-left py-3 px-4 font-medium">Transaction Type</th>
                          <th className="text-left py-3 px-4 font-medium">Description</th>
                          <th className="text-left py-3 px-4 font-medium">Status</th>
                          <th className="text-left py-3 px-4 font-medium">Date/Time</th>
                        </tr>
                      </thead>
                      <tbody>
                        {searchedTransactions.map((transaction) => (
                          <motion.tr 
                            key={transaction.id} 
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="border-b hover:bg-muted/50 transition-colors"
                          >
                            <td className="py-3 px-4 font-mono text-sm">{transaction.senderAccountNumber}</td>
                            <td className="py-3 px-4 font-mono text-sm">{transaction.receiverAccountNumber}</td>
                            <td className="py-3 px-4 font-semibold">₹{Number(transaction.amount).toLocaleString()}</td>
                            <td className="py-3 px-4 capitalize">{transaction.transactionType}</td>
                            <td className="py-3 px-4 text-sm">{transaction.description || '-'}</td>
                            <td className="py-3 px-4">
                              <span className="px-2 py-1 rounded-full text-xs bg-secondary/20 text-secondary">
                                {transaction.status}
                              </span>
                            </td>
                            <td className="py-3 px-4 text-sm text-muted-foreground">
                              {new Date(transaction.timestamp).toLocaleString()}
                            </td>
                          </motion.tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </motion.div>
            )}
          </CardContent>
        </Card>

      </div>
    </DashboardLayout>
  );
};

export default Transactions;
