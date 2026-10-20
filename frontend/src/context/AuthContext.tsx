import { useEffect, useState, type ReactNode } from 'react';
import { AuthContext } from './auth-context';

const API_URL = import.meta.env.VITE_API_URL ?? '';
const AUTH_CHECK_INTERVAL = 30 * 1000;

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * Checks whether the current JWT cookie represents an authenticated user.
   */
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
        }
      } catch {
        // Keep the current authentication state if the server is temporarily unavailable.
      } finally {
        isChecking = false;

        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    // Check the session when the application starts.
    checkAuthentication();

    // Periodically check whether the session is still valid.
    const intervalId = window.setInterval(
      checkAuthentication,
      AUTH_CHECK_INTERVAL
    );

    // Recheck the session when the user returns to the application.
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        checkAuthentication();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
      document.removeEventListener(
        'visibilitychange',
        handleVisibilityChange
      );
    };
  }, []);

  /**
   * Logs the user in through the backend.
   * The backend stores the JWT in an HttpOnly cookie.
   */
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

    setIsAuthenticated(true);
  };

  /**
   * Logs the user out through the backend.
   * The backend clears the HttpOnly JWT cookie.
   */
  const logout = async () => {
    try {
      await fetch(`${API_URL}/api/auth/logout`, {
        method: 'DELETE',
        credentials: 'include',
      });
    } finally {
      setIsAuthenticated(false);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};