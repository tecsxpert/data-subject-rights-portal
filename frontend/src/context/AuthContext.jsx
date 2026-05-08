// src/context/AuthContext.jsx
import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore session from localStorage on mount
  useEffect(() => {
    const token    = localStorage.getItem('dsr_token');
    const userData = localStorage.getItem('dsr_user');
    if (token && userData) {
      try { setUser(JSON.parse(userData)); } catch { /* ignore */ }
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (credentials) => {
    const data = await authApi.login(credentials);
    const user = { username: data.username, role: data.role };
    localStorage.setItem('dsr_token', data.token);
    localStorage.setItem('dsr_user',  JSON.stringify(user));
    setUser(user);
    return data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('dsr_token');
    localStorage.removeItem('dsr_user');
    setUser(null);
  }, []);

  const isAdmin   = user?.role === 'ADMIN';
  const isManager = user?.role === 'MANAGER' || isAdmin;

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, isAdmin, isManager }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
};
