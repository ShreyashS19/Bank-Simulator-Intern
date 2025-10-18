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
import { Search, UserPlus, Loader2, Eye, Edit, Trash2 } from "lucide-react";
import { Customer } from "@/data/dummyCustomers";
import { customerApi } from "@/services/dummyApi";
import { CustomerViewModal } from "@/components/CustomerViewModal";
import { CustomerEditModal } from "@/components/CustomerEditModal";

const Customers = () => {
  const [aadharSearch, setAadharSearch] = useState("");
  const [searchedCustomer, setSearchedCustomer] = useState<Customer | null>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [searchNotFound, setSearchNotFound] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [deletingCustomer, setDeletingCustomer] = useState<Customer | null>(null);
  const [viewingCustomer, setViewingCustomer] = useState<Customer | null>(null);
  const [formData, setFormData] = useState({
    name: "",
    phoneNumber: "",
    email: "",
    address: "",
    customerPin: "",
    aadharNumber: "",
    dob: "",
    status: "active"
  });

  const handleSearch = async () => {
    if (!aadharSearch.trim()) {
      toast.error("Please enter an Aadhaar number");
      return;
    }
    
    setIsSearching(true);
    setSearchNotFound(false);
    setSearchedCustomer(null);
    
    try {
      const customer = await customerApi.getByAadharNumber(aadharSearch.trim());
      if (customer) {
        setSearchedCustomer(customer);
      } else {
        setSearchNotFound(true);
      }
    } catch (error) {
      toast.error("Failed to search customer");
      console.error(error);
    } finally {
      setIsSearching(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await customerApi.create(formData);
      toast.success("Customer created successfully!");
      handleReset();
      // Clear search results after creating a new customer
      setSearchedCustomer(null);
      setAadharSearch("");
      setSearchNotFound(false);
    } catch (error) {
      toast.error("Failed to create customer");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = async (customer: Customer) => {
    setIsLoading(true);
    try {
      await customerApi.update(customer.id, customer);
      toast.success("Customer updated successfully!");
      setEditingCustomer(null);
      // Refresh the searched customer data
      if (searchedCustomer?.id === customer.id) {
        setSearchedCustomer(customer);
      }
    } catch (error) {
      toast.error("Failed to update customer");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deletingCustomer) return;
    setIsLoading(true);
    try {
      await customerApi.delete(deletingCustomer.id);
      toast.success("Customer deleted successfully!");
      setDeletingCustomer(null);
      // Clear search results if the deleted customer was being displayed
      if (searchedCustomer?.id === deletingCustomer.id) {
        setSearchedCustomer(null);
        setAadharSearch("");
      }
    } catch (error) {
      toast.error("Failed to delete customer");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    setFormData({
      name: "",
      phoneNumber: "",
      email: "",
      address: "",
      customerPin: "",
      aadharNumber: "",
      dob: "",
      status: "active"
    });
  };


  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Customer Management</h1>
          <p className="text-muted-foreground mt-1">Create and manage customer accounts</p>
        </div>

        {/* Create Customer Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <UserPlus className="h-5 w-5" />
              Create New Customer
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Full Name</Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phoneNumber">Phone Number</Label>
                  <Input
                    id="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="address">Address</Label>
                  <Input
                    id="address"
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="customerPin">Customer PIN</Label>
                  <Input
                    id="customerPin"
                    type="password"
                    value={formData.customerPin}
                    onChange={(e) => setFormData({ ...formData, customerPin: e.target.value })}
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
                  <Label htmlFor="dob">Date of Birth</Label>
                  <Input
                    id="dob"
                    type="date"
                    value={formData.dob}
                    onChange={(e) => setFormData({ ...formData, dob: e.target.value })}
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
                  {isLoading ? <><Loader2 className="h-4 w-4 animate-spin mr-2" /> Creating...</> : "Create Customer"}
                </Button>
                <Button type="button" variant="outline" onClick={handleReset}>Reset</Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Search Customer */}
        <Card>
          <CardHeader>
            <CardTitle>Search Customer</CardTitle>
            <p className="text-sm text-muted-foreground">Enter Aadhaar number to find customer details</p>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-3">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Enter Aadhaar Number (e.g., 1234 5678 9012)"
                  value={aadharSearch}
                  onChange={(e) => setAadharSearch(e.target.value)}
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
                  No customer found with this Aadhaar number.
                </AlertDescription>
              </Alert>
            )}

            {searchedCustomer && (
              <Card className="border-2">
                <CardHeader>
                  <CardTitle className="text-lg">Customer Details</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <Label className="text-muted-foreground">ID</Label>
                      <p className="font-medium">{searchedCustomer.id}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Name</Label>
                      <p className="font-medium">{searchedCustomer.name}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Email</Label>
                      <p className="font-medium">{searchedCustomer.email}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Phone</Label>
                      <p className="font-medium">{searchedCustomer.phoneNumber}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Aadhaar</Label>
                      <p className="font-medium">{searchedCustomer.aadharNumber}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">DOB</Label>
                      <p className="font-medium">{new Date(searchedCustomer.dob).toLocaleDateString()}</p>
                    </div>
                    <div>
                      
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Status</Label>
                      <p className="font-medium capitalize">{searchedCustomer.status}</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => setViewingCustomer(searchedCustomer)}>
                      <Eye className="h-4 w-4 mr-2" />
                      View Details
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => setEditingCustomer(searchedCustomer)}>
                      <Edit className="h-4 w-4 mr-2" />
                      Edit
                    </Button>
                    <Button variant="destructive" size="sm" onClick={() => setDeletingCustomer(searchedCustomer)}>
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

      <CustomerViewModal 
        customer={viewingCustomer}
        open={!!viewingCustomer}
        onClose={() => setViewingCustomer(null)}
        onEdit={(customer) => setEditingCustomer(customer)}
      />

      <CustomerEditModal 
        customer={editingCustomer}
        open={!!editingCustomer}
        isLoading={isLoading}
        onClose={() => setEditingCustomer(null)}
        onSave={handleEdit}
        onChange={(customer) => setEditingCustomer(customer)}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deletingCustomer} onOpenChange={() => setDeletingCustomer(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete the customer "{deletingCustomer?.name}". This action cannot be undone.
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

export default Customers;
