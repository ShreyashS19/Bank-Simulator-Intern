import { ReactNode, useEffect, useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { LayoutDashboard, Users, CreditCard, ArrowLeftRight, LogOut, Menu, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { cn } from "@/lib/utils";

interface DashboardLayoutProps {
  children: ReactNode;
}


const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isCollapsed, setIsCollapsed] = useState(false);
//  const [isCollapsed, setIsCollapsed] = useState(() => {
//   const saved = localStorage.getItem("sidebarCollapsed");
//   return saved === "true";
// });

  useEffect(() => {
    const isAuthenticated = localStorage.getItem("isAuthenticated");
    if (!isAuthenticated) {
      navigate("/login");
    }
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("isAuthenticated");
    toast.success("Logged out successfully");
    navigate("/login");
  };

  const toggleSidebar = () => {
    setIsCollapsed(!isCollapsed);
  };
// const toggleSidebar = () => {
//   const newState = !isCollapsed;
//   setIsCollapsed(newState);
//   localStorage.setItem("sidebarCollapsed", String(newState));
// };
  const navItems = [
    { path: "/dashboard", icon: LayoutDashboard, label: "Dashboard" },
    { path: "/customers", icon: Users, label: "Customers" },
    { path: "/accounts", icon: CreditCard, label: "Accounts" },
    { path: "/transactions", icon: ArrowLeftRight, label: "Transactions" },
  ];

  return (
    <div className="flex min-h-screen w-full bg-background">
      {/* Collapsible Sidebar */}
      <motion.aside
        initial={false}
        animate={{ width: isCollapsed ? "80px" : "256px" }}
        transition={{ duration: 0.3, ease: "easeInOut" }}
        className="bg-sidebar border-r border-sidebar-border flex flex-col relative"
      >
        {/* Header with Toggle Button */}
        <div className="p-6 border-b border-sidebar-border flex items-center justify-between">
          <AnimatePresence mode="wait">
            {!isCollapsed && (
              <motion.h1
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.2 }}
                className="text-2xl font-bold text-sidebar-foreground"
              >
                BankSim
              </motion.h1>
            )}
          </AnimatePresence>
          
          <Button
            variant="ghost"
            size="icon"
            onClick={toggleSidebar}
            className="text-sidebar-foreground hover:bg-sidebar-accent"
          >
            {isCollapsed ? <Menu className="h-5 w-5" /> : <X className="h-5 w-5" />}
          </Button>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 p-4 space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link key={item.path} to={item.path}>
                <Button
                  variant="ghost"
                  className={cn(
                    "w-full text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                    isActive && "bg-sidebar-accent text-sidebar-accent-foreground",
                    isCollapsed ? "justify-center px-2" : "justify-start"
                  )}
                  title={isCollapsed ? item.label : undefined}
                >
                  <Icon className={cn("h-5 w-5", !isCollapsed && "mr-3")} />
                  <AnimatePresence mode="wait">
                    {!isCollapsed && (
                      <motion.span
                        initial={{ opacity: 0, width: 0 }}
                        animate={{ opacity: 1, width: "auto" }}
                        exit={{ opacity: 0, width: 0 }}
                        transition={{ duration: 0.2 }}
                      >
                        {item.label}
                      </motion.span>
                    )}
                  </AnimatePresence>
                </Button>
              </Link>
            );
          })}
        </nav>

        {/* Logout Button */}
        <div className="p-4 border-t border-sidebar-border">
          <Button
            variant="ghost"
            onClick={handleLogout}
            className={cn(
              "w-full text-sidebar-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
              isCollapsed ? "justify-center px-2" : "justify-start"
            )}
            title={isCollapsed ? "Logout" : undefined}
          >
            <LogOut className={cn("h-5 w-5", !isCollapsed && "mr-3")} />
            <AnimatePresence mode="wait">
              {!isCollapsed && (
                <motion.span
                  initial={{ opacity: 0, width: 0 }}
                  animate={{ opacity: 1, width: "auto" }}
                  exit={{ opacity: 0, width: 0 }}
                  transition={{ duration: 0.2 }}
                >
                  Logout
                </motion.span>
              )}
            </AnimatePresence>
          </Button>
        </div>
      </motion.aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
          className="p-8"
        >
          {children}
        </motion.div>
      </main>
    </div>
  );
};

export default DashboardLayout;
