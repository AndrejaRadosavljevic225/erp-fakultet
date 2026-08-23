import type { ReactNode } from 'react';
import { useAuth } from './AuthContext';

/**
 * Prikazuje decu samo ako korisnik ima jednu od zadatih rola.
 * Koristi se za sakrivanje dugmadi (npr. brisanje sme samo ADMIN).
 */
export function RoleGate({ roles, children }: { roles: string[]; children: ReactNode }) {
  const { hasRole } = useAuth();
  return hasRole(...roles) ? <>{children}</> : null;
}
