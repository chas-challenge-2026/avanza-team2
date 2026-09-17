import { useState, type ReactNode } from 'react';
import { AuthContext, AuthStorageKey } from './auth-context';

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(
    () => localStorage.getItem(AuthStorageKey) !== null,
  );

  const login = () => {
    localStorage.setItem(AuthStorageKey, 'mock-token');
    setIsAuthenticated(true);
  };

  const logout = () => {
    localStorage.removeItem(AuthStorageKey);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};