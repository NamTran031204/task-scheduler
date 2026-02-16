import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

interface AuthContextType {
  isAuthenticated: boolean;
  login: (token: string, userId: string) => void;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    
    if (token && userId) {
      setIsAuthenticated(true);
      
      if (location.pathname === '/login') {
        const from = location.state?.from?.pathname || '/dashboard';
        navigate(from, { replace: true });
      }
    } else {
      if (location.pathname !== '/login' && location.pathname !== '/register') {
        navigate('/login', { 
          replace: true,
          state: { from: location }
        });
      }
    }
    
    setLoading(false);
  }, [navigate, location]);

  const login = useCallback((token: string, userId: string) => {
    localStorage.setItem('token', token);
    localStorage.setItem('userId', userId);
    setIsAuthenticated(true);
    
    const from = location.state?.from?.pathname || '/dashboard';
    navigate(from, { replace: true });
  }, [navigate, location.state]);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    setIsAuthenticated(false);
    navigate('/login', { replace: true });
  }, [navigate]);

  const value = {
    isAuthenticated,
    login,
    logout,
    loading
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
  
  
  
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
