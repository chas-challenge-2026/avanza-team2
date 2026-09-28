import { useEffect, useState, type ReactNode } from 'react';
import { AuthContext } from './auth-context';

const API_URL = 'http://localhost:8082';

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * Checks with the backend whether the current HttpOnly JWT cookie
   * represents an authenticated user.
   */
  useEffect(() => {
    const checkAuthentication = async () => {
      try {
        const response = await fetch(`${API_URL}/api/auth/me`, {
          method: 'GET',

          // Allow the browser to send the HttpOnly JWT cookie.
          credentials: 'include',
        });

        setIsAuthenticated(response.ok);
      } catch {
        setIsAuthenticated(false);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuthentication();
  }, []);

  /**
   * Logs the user in through the backend.
   *
   * The backend sets the JWT as an HttpOnly cookie.
   * The JWT is therefore never stored in localStorage or accessible
   * to JavaScript.
   */
  const login = async (email: string, password: string) => {
    const response = await fetch(`${API_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },

      // Allow the backend to set the HttpOnly cookie.
      credentials: 'include',

      body: JSON.stringify({
        email,
        password,
      }),
    });

    if (!response.ok) {
      throw new Error('Login failed');
    }

    setIsAuthenticated(true);
  };

  /**
   * Logs the user out through the backend.
   *
   * The backend clears the HttpOnly JWT cookie.
   */
  const logout = async () => {
    try {
      await fetch(`${API_URL}/api/auth/logout`, {
        method: 'DELETE',

        // Send the JWT cookie so the backend can clear it.
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