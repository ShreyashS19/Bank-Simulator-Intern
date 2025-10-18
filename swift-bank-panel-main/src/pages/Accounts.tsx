import { useState } from "react";
import DashboardLayout from "@/components/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { toast } from "sonner";
import { Search, CreditCard, Loader2, Eye, Edit, Trash2 } from "lucide-react";
import { Account } from "@/data/dummyAccounts";
import { accountApi } from "@/services/dummyApi";
import { AccountViewModal } from "@/components/AccountViewModal";
import { AccountEditModal } from "@/components/AccountEditModal";

const Accounts = () => {
  const [accountSearch, setAccountSearch] = useState("");
  const [searchedAccount, setSearchedAccount] = useState<Account | null>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [searchNotFound, setSearchNotFound] = useState(false);
  const [editingAccount, setEditingAccount] = useState<Account | null>(null);
  const [deletingAccount, setDeletingAccount] = useState<Account | null>(null);
  const [viewingAccount, setViewingAccount] = useState<Account | null>(null);
  const [formData, setFormData] = useState({
    accountNumber: "",
    customerName: "",
    accountType: "Savings" as "Savings" | "Current",
    balance: "",
    aadharNumber: "",
    ifscCode: "",
    bankName: "",
    nameOnAccount: "",
    status: "active"
  });

  const handleSearch = async () => {
    if (!accountSearch.trim()) {
      toast.error("Please enter an account number");
      return;
    }
    
    setIsSearching(true);
    setSearchNotFound(false);
    setSearchedAccount(null);
    
    try {
      const account = await accountApi.getByAccountNumber(accountSearch.trim());
      if (account) {
        setSearchedAccount(account);
      } else {
        setSearchNotFound(true);
      }
    } catch (error) {
      toast.error("Failed to search account");
      console.error(error);
    } finally {
      setIsSearching(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await accountApi.create({
        ...formData,
        balance: parseFloat(formData.balance)
      });
      toast.success("Account created successfully!");
      handleReset();
      // Clear search results after creating a new account
      setSearchedAccount(null);
      setAccountSearch("");
      setSearchNotFound(false);
    } catch (error) {
      toast.error("Failed to create account");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = async (account: Account) => {
    setIsLoading(true);
    try {
      await accountApi.update(account.id, account);
      toast.success("Account updated successfully!");
      setEditingAccount(null);
      // Refresh the searched account data
      if (searchedAccount?.id === account.id) {
        setSearchedAccount(account);
      }
    } catch (error) {
      toast.error("Failed to update account");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deletingAccount) return;
    setIsLoading(true);
    try {
      await accountApi.delete(deletingAccount.id);
      toast.success("Account deleted successfully!");
      setDeletingAccount(null);
      // Clear search results if the deleted account was being displayed
      if (searchedAccount?.id === deletingAccount.id) {
        setSearchedAccount(null);
        setAccountSearch("");
      }
    } catch (error) {
      toast.error("Failed to delete account");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    setFormData({
      accountNumber: "",
      customerName: "",
      accountType: "Savings",
      balance: "",
      aadharNumber: "",
      ifscCode: "",
      bankName: "",
      nameOnAccount: "",
      status: "active"
    });
  };


  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Account Management</h1>
          <p className="text-muted-foreground mt-1">Create and manage bank accounts</p>
        </div>

        {/* Create Account Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="h-5 w-5" />
              Create New Account
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="accountNumber">Account Number</Label>
                  <Input
                    id="accountNumber"
                    value={formData.accountNumber}
                    onChange={(e) => setFormData({ ...formData, accountNumber: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="customerName">Customer Name</Label>
                  <Input
                    id="customerName"
                    value={formData.customerName}
                    onChange={(e) => setFormData({ ...formData, customerName: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="nameOnAccount">Name on Account</Label>
                  <Input
                    id="nameOnAccount"
                    value={formData.nameOnAccount}
                    onChange={(e) => setFormData({ ...formData, nameOnAccount: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="accountType">Account Type</Label>
                  <Select value={formData.accountType} onValueChange={(value: "Savings" | "Current") => setFormData({ ...formData, accountType: value })}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Savings">Savings</SelectItem>
                      <SelectItem value="Current">Current</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="balance">Balance</Label>
                  <Input
                    id="balance"
                    type="number"
                    step="0.01"
                    value={formData.balance}
                    onChange={(e) => setFormData({ ...formData, balance: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="aadharNumber">Aadhar Number</Label>
                  <Input
                    id="aadharNumber"
                    value={formData.aadharNumber}
                    onChange={(e) => setFormData({ ...formData, aadharNumber: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="bankName">Bank Name</Label>
                  <Input
                    id="bankName"
                    value={formData.bankName}
                    onChange={(e) => setFormData({ ...formData, bankName: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="ifscCode">IFSC Code</Label>
                  <Input
                    id="ifscCode"
                    value={formData.ifscCode}
                    onChange={(e) => setFormData({ ...formData, ifscCode: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="status">Status</Label>
                  <Select value={formData.status} onValueChange={(value) => setFormData({ ...formData, status: value })}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="active">Active</SelectItem>
                      <SelectItem value="inactive">Inactive</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              <div className="flex gap-3">
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? <><Loader2 className="h-4 w-4 animate-spin mr-2" /> Creating...</> : "Create Account"}
                </Button>
                <Button type="button" variant="outline" onClick={handleReset}>Reset</Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Search Account */}
        <Card>
          <CardHeader>
            <CardTitle>Search Account</CardTitle>
            <p className="text-sm text-muted-foreground">Enter Account Number to find account details</p>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-3">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Enter Account Number (e.g., ACC001234567890)"
                  value={accountSearch}
                  onChange={(e) => setAccountSearch(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  className="pl-9"
                />
              </div>
              <Button onClick={handleSearch} disabled={isSearching}>
                {isSearching ? <><Loader2 className="h-4 w-4 animate-spin mr-2" /> Searching...</> : <><Search className="h-4 w-4 mr-2" /> Search</>}
              </Button>
            </div>

            {searchNotFound && (
              <Alert>
                <AlertDescription>
                  No account found with this account number.
                </AlertDescription>
              </Alert>
            )}

            {searchedAccount && (
              <Card className="border-2">
                <CardHeader>
                  <CardTitle className="text-lg">Account Details</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <Label className="text-muted-foreground">Account ID</Label>
                      <p className="font-medium">{searchedAccount.id}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Account Number</Label>
                      <p className="font-medium">{searchedAccount.accountNumber}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Customer Name</Label>
                      <p className="font-medium">{searchedAccount.customerName}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Name on Account</Label>
                      <p className="font-medium">{searchedAccount.nameOnAccount}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Account Type</Label>
                      <p className="font-medium">{searchedAccount.accountType}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Balance</Label>
                      <p className="font-medium">₹{searchedAccount.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Bank Name</Label>
                      <p className="font-medium">{searchedAccount.bankName}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">IFSC Code</Label>
                      <p className="font-medium">{searchedAccount.ifscCode}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Aadhaar Number</Label>
                      <p className="font-medium">{searchedAccount.aadharNumber}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Status</Label>
                      <p className="font-medium capitalize">{searchedAccount.status}</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => setViewingAccount(searchedAccount)}>
                      <Eye className="h-4 w-4 mr-2" />
                      View Details
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => setEditingAccount(searchedAccount)}>
                      <Edit className="h-4 w-4 mr-2" />
                      Edit
                    </Button>
                    <Button variant="destructive" size="sm" onClick={() => setDeletingAccount(searchedAccount)}>
                      <Trash2 className="h-4 w-4 mr-2" />
                      Delete
                    </Button>
                  </div>
                </CardContent>
              </Card>
            )}
          </CardContent>
        </Card>
      </div>

      <AccountViewModal 
        account={viewingAccount}
        open={!!viewingAccount}
        onClose={() => setViewingAccount(null)}
        onEdit={(account) => setEditingAccount(account)}
      />

      <AccountEditModal 
        account={editingAccount}
        open={!!editingAccount}
        isLoading={isLoading}
        onClose={() => setEditingAccount(null)}
        onSave={handleEdit}
        onChange={(account) => setEditingAccount(account)}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deletingAccount} onOpenChange={() => setDeletingAccount(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete the account "{deletingAccount?.accountNumber}". This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              {isLoading ? <><Loader2 className="h-4 w-4 animate-spin mr-2" /> Deleting...</> : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </DashboardLayout>
  );
};

export default Accounts;
