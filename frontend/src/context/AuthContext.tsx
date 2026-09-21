import { useState, type ReactNode } from 'react';
import { AuthContext, AuthStorageKey } from './auth-context';

const API_URL = 'http://localhost:8082';

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(
    () => localStorage.getItem(AuthStorageKey) !== null,
  );

  const login = async (email: string, password: string) => {
    const response = await fetch(`${API_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email,
        password,
      }),
    });

    if (!response.ok) {
      throw new Error('Login failed');
    }

    const data = await response.json();

    localStorage.setItem(AuthStorageKey, data.token);
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