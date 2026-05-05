import React, { createContext, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login as authServiceLogin, logout as authServiceLogout, getToken } from '../services/authService';

interface AuthContextType {
  token: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [token, setToken] = useState<string | null>(getToken());
  const navigate = useNavigate();

  const login = async (username: string, password: string) => {
    await authServiceLogin(username, password);
    setToken(getToken());
    navigate('/');
  };

  const logout = async () => {
    await authServiceLogout();
    setToken(null);
    navigate('/login');
  };

  return (
    <AuthContext.Provider value={{ token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
