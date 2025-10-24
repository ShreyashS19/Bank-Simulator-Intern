import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import { LogIn, Loader2, Shield } from "lucide-react";
import { authService } from "@/services/authService";

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });
  const [loading, setLoading] = useState(false);

  // Admin credentials
  const ADMIN_EMAIL = 'admin@bank.com';
  const ADMIN_PASSWORD = 'Admin@123';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.email || !formData.password) {
      toast.error("Please fill in all fields");
      return;
    }

    setLoading(true);

    try {
      // ✅ STEP 1: Check if credentials match admin
      if (formData.email === ADMIN_EMAIL && formData.password === ADMIN_PASSWORD) {
        // Admin login
        localStorage.setItem('user', JSON.stringify({ 
          email: ADMIN_EMAIL, 
          role: 'admin',
          fullName: 'System Administrator'
        }));
        localStorage.setItem('isAuthenticated', 'true');
        localStorage.setItem('isAdmin', 'true');
        localStorage.setItem('hasCustomerRecord', 'false');
        
        toast.success('Admin login successful!', {
          description: 'Redirecting to admin dashboard...',
          icon: <Shield className="h-4 w-4" />
        });
        
        // Small delay for better UX
        setTimeout(() => {
          navigate('/admin');
        }, 500);
        
        setLoading(false);
        return;
      }

      // ✅ STEP 2: Regular user login authentication
      const response = await authService.login({
        email: formData.email,
        password: formData.password
      });

      if (response.success) {
        // Store user data and authentication status
        localStorage.setItem('user', JSON.stringify(response.data));
        localStorage.setItem('isAuthenticated', 'true');
        localStorage.setItem('isAdmin', 'false'); // Regular user
        
        // ✅ STEP 3: Check if user has a customer record
        try {
          const customerCheck = await authService.checkCustomerExists(formData.email);
          
          // Store customer record status
          const hasCustomerRecord = customerCheck.success && customerCheck.data.hasCustomerRecord;
          localStorage.setItem('hasCustomerRecord', String(hasCustomerRecord));
          
          console.log('Customer record status:', hasCustomerRecord);
          
          toast.success("Login successful!");
          
          // Redirect based on customer record status
          if (hasCustomerRecord) {
            // User has customer record - redirect to main dashboard
            console.log('User has customer record, redirecting to dashboard');
            navigate("/dashboard");
          } else {
            // User doesn't have customer record - can access customers page
            console.log('User does not have customer record, redirecting to dashboard');
            navigate("/dashboard");
          }
        } catch (customerCheckError: any) {
          // If customer check fails, default to no customer record
          console.warn('Customer check failed, defaulting to no customer record:', customerCheckError);
          localStorage.setItem('hasCustomerRecord', 'false');
          navigate("/dashboard");
        }
      } else {
        toast.error(response.message || "Login failed");
      }
    } 
    catch (error: any) {
      console.error("Login error:", error);
      
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else if (error.response?.status === 401) {
        toast.error("Invalid email or password");
      } else if (error.code === 'ERR_NETWORK') {
        toast.error("Cannot connect to server. Ensure backend is running.");
      } else {
        toast.error("Login failed. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        className="w-full max-w-md"
      >
        <div className="text-center mb-8">
          <Link to="/" className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
            Bank Simulation
          </Link>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-2xl">Welcome Back</CardTitle>
            <CardDescription>Enter your credentials to access your account</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="Enter your email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="Enter your password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  required
                />
              </div>
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Logging in...
                  </>
                ) : (
                  <>
                    <LogIn className="mr-2 h-4 w-4" />
                    Login
                  </>
                )}
              </Button>
            </form>
            
            {/* Admin hint - Optional */}
            <div className="mt-4 p-3 bg-muted/50 rounded-lg border border-border">
              <p className="text-xs text-muted-foreground text-center flex items-center justify-center gap-2">
                <Shield className="h-3 w-3" />
                Admin Access: admin@bank.com
              </p>
            </div>

            <div className="mt-4 text-center text-sm">
              <span className="text-muted-foreground">New user? </span>
              <Link to="/signup" className="text-primary hover:underline font-medium">
                Sign Up
              </Link>
            </div>
          </CardContent>
        </Card>
      </motion.div>
    </div>
  );
};

export default Login;
