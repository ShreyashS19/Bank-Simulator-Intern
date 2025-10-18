import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Account } from "@/data/dummyAccounts";
import { Loader2 } from "lucide-react";

interface AccountEditModalProps {
  account: Account | null;
  open: boolean;
  isLoading: boolean;
  onClose: () => void;
  onSave: (account: Account) => void;
  onChange: (account: Account) => void;
}

export const AccountEditModal = ({ account, open, isLoading, onClose, onSave, onChange }: AccountEditModalProps) => {
  if (!account) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Edit Account</DialogTitle>
          <DialogDescription>Update account information</DialogDescription>
        </DialogHeader>
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="edit-account-number">Account Number</Label>
            <Input
              id="edit-account-number"
              value={account.accountNumber}
              onChange={(e) => onChange({ ...account, accountNumber: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-customer-name">Customer Name</Label>
            <Input
              id="edit-customer-name"
              value={account.customerName}
              onChange={(e) => onChange({ ...account, customerName: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-name-on-account">Name on Account</Label>
            <Input
              id="edit-name-on-account"
              value={account.nameOnAccount}
              onChange={(e) => onChange({ ...account, nameOnAccount: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-account-type">Account Type</Label>
            <Select 
              value={account.accountType} 
              onValueChange={(value: "Savings" | "Current") => onChange({ ...account, accountType: value })}
            >
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
            <Label htmlFor="edit-balance">Balance</Label>
            <Input
              id="edit-balance"
              type="number"
              step="0.01"
              value={account.balance}
              onChange={(e) => onChange({ ...account, balance: parseFloat(e.target.value) })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-bank-name">Bank Name</Label>
            <Input
              id="edit-bank-name"
              value={account.bankName}
              onChange={(e) => onChange({ ...account, bankName: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-ifsc">IFSC Code</Label>
            <Input
              id="edit-ifsc"
              value={account.ifscCode}
              onChange={(e) => onChange({ ...account, ifscCode: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-aadhar-account">Aadhar Number</Label>
            <Input
              id="edit-aadhar-account"
              value={account.aadharNumber}
              onChange={(e) => onChange({ ...account, aadharNumber: e.target.value })}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-account-status">Status</Label>
            <Select 
              value={account.status} 
              onValueChange={(value) => onChange({ ...account, status: value })}
            >
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
        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={isLoading}>Cancel</Button>
          <Button onClick={() => onSave(account)} disabled={isLoading}>
            {isLoading ? <><Loader2 className="h-4 w-4 animate-spin mr-2" /> Updating...</> : "Save Changes"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
