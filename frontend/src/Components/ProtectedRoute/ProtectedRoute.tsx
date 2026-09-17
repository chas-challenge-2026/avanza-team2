import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../../context/useAuth';

export const ProtectedRoute = ({ children }: { children: ReactNode }) => {
  const { isAuthenticated } = useAuth() as { isAuthenticated: boolean };

  if (!isAuthenticated) {
    return <Navigate to="/Loggain" replace />;
  }

  return <>{children}</>;
};
