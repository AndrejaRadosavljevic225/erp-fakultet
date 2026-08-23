import { Center, Loader } from '@mantine/core';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * Propusta samo prijavljene korisnike. Ako je zadat `roles`, propusta samo te role
 * (ista pravila kao @PreAuthorize na backendu — UI samo ne nudi ono sto bi vratilo 403).
 */
export function ProtectedRoute({ roles }: { roles?: string[] }) {
  const { user, loading, hasRole } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <Center h="100vh">
        <Loader />
      </Center>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  if (roles && !hasRole(...roles)) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
