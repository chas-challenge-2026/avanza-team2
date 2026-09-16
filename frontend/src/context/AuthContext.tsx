import { createContext, useContext, useState, type ReactNode } from 'react';

interface AuthContextValue {
  isAuthenticated: boolean;
  login: () => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const AuthStorageKey = 'avanza_auth_token';

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(
    () => localStorage.getItem(AuthStorageKey) !== null,
  );

  const login = () => {
    // Placeholder until the real backend issues a token — swap this for
    // storing the actual JWT once auth is wired up.
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

