import { StrictMode } from "react";
import type { ReactNode } from "react";
import { createRoot } from "react-dom/client";
import { Route, BrowserRouter as Router, Routes, Navigate, useLocation } from "react-router-dom"; 
import { AuthProvider, useAuth } from "./contexts/AuthContext"; 
import "./index.css";
import RegisterPage from "./pages/userAuth/Register";
import Login from "./pages/userAuth/Login";
import Dashboard from "./components/dashboard/Dashboard";
import UserProfile from "./components/user/UserProfile";

const ProtectedRoute = ({ children }: { children: ReactNode }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) return null;

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

const PublicRoute = ({ children }: { children: ReactNode }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) return null;

  if (isAuthenticated) {
    return <Navigate to="/dashboard" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

const HomeRedirect = () => {
  return <Navigate to="/login" replace />;
};

const AppRoutes = () => (
  <Routes>
    <Route path="/" element={<HomeRedirect />} />
    
    <Route path="/login" element={
      <PublicRoute>
        <Login />
      </PublicRoute>
    } />
    
    <Route path="/register" element={
      <PublicRoute>
        <RegisterPage />
      </PublicRoute>
    } />
    
    <Route path="/dashboard" element={
      <ProtectedRoute>
        <Dashboard />
      </ProtectedRoute>
    } />
    <Route path="/profile" element={<UserProfile />} />
  </Routes>
);

const App = () => (
  <AuthProvider>
    <AppRoutes />
  </AuthProvider>
);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Router>
      <App />
    </Router>
  </StrictMode>
);
