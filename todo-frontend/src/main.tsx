import { StrictMode } from "react";
import type { ReactNode } from "react";
import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";
import { Route, BrowserRouter as Router, Routes, Navigate, useLocation } from "react-router-dom";
import { ConfigProvider, theme as antdTheme } from "antd";
import viVN from "antd/locale/vi_VN";
import { store } from "./store";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import "./index.css";
import RegisterPage from "./pages/userAuth/Register";
import Login from "./pages/userAuth/Login";
import Dashboard from "./components/dashboard/Dashboard";
import UserProfile from "./components/user/UserProfile";
import CalendarView from "./components/CalendarView";

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
    <Route path="/calendar" element={
      <ProtectedRoute>
        <CalendarView />
      </ProtectedRoute>
    } />
    <Route path="/profile" element={
      <ProtectedRoute>
        <UserProfile />
      </ProtectedRoute>
    } />
  </Routes>
);

const App = () => (
  <ConfigProvider
    locale={viVN}
    theme={{
      algorithm: antdTheme.defaultAlgorithm,
      token: {
        borderRadius: 10,
        colorBgLayout: "#f5f7fa",
        motionDurationFast: "0.14s",
        motionDurationMid: "0.24s",
        motionDurationSlow: "0.32s",
        motionEaseInOut: "cubic-bezier(0.65, 0, 0.35, 1)",
        motionEaseOut: "cubic-bezier(0.16, 1, 0.3, 1)",
      },
      components: {
        Layout: { headerHeight: 64, headerPadding: "0 24px" },
        Menu: { itemBorderRadius: 8, iconSize: 18 },
        Modal: { motionDurationSlow: "0.3s" },
        Button: { motionDurationSlow: "0.22s" },
      },
    }}
  >
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  </ConfigProvider>
);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Provider store={store}>
      <Router>
        <App />
      </Router>
    </Provider>
  </StrictMode>
);
