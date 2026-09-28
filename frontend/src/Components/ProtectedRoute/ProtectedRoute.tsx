import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../../context/useAuth';

export const ProtectedRoute = ({ children }: { children: ReactNode }) => {
  const { isAuthenticated, isLoading } = useAuth() as {
    isAuthenticated: boolean;
    isLoading: boolean;
  };

  // Wait for the backend to check the HttpOnly JWT cookie.
  if (isLoading) {
    return null;
  }

  // Redirect unauthenticated users to the login page.
  if (!isAuthenticated) {
    return <Navigate to="/Loggain" replace />;
  }

  return <>{children}</>;
};