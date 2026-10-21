import { useEffect, useState, type ReactNode } from 'react';
import { AuthContext, type AuthUser } from './auth-context';

const API_URL = import.meta.env.VITE_API_URL ?? '';
const AUTH_CHECK_INTERVAL = 30 * 1000;

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    let isMounted = true;
    let isChecking = false;

    const checkAuthentication = async () => {
      if (isChecking) return;
      isChecking = true;

      try {
        const response = await fetch(`${API_URL}/api/auth/me`, {
          method: 'GET',
          credentials: 'include',
        });

        if (!isMounted) return;

        if (response.ok) {
          setIsAuthenticated(true);
        } else if (response.status === 401 || response.status === 403) {
          setIsAuthenticated(false);
          setUser(null);
        }
      } catch {
        // Keep the current authentication state during temporary network failures.
      } finally {
        isChecking = false;
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void checkAuthentication();

    const intervalId = window.setInterval(
      () => void checkAuthentication(),
      AUTH_CHECK_INTERVAL,
    );
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        void checkAuthentication();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  const login = async (email: string, password: string) => {
    const response = await fetch(`${API_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      throw new Error('Login failed');
    }

    const data = (await response.json()) as { name: string; email: string };
    setUser({ name: data.name, email: data.email });
    setIsAuthenticated(true);
  };

  const logout = async () => {
    try {
      await fetch(`${API_URL}/api/auth/logout`, {
        method: 'DELETE',
        credentials: 'include',
      });
    } finally {
      setIsAuthenticated(false);
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        user,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
