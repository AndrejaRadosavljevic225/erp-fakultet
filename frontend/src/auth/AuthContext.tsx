import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { authApi } from '../api/hr';
import { getStoredToken, setStoredToken, setUnauthorizedHandler } from '../api/client';
import type { CurrentUser } from '../types/hr';

interface AuthContextValue {
  user: CurrentUser | null;
  /** true dok traje pocetna provera postojeceg tokena */
  loading: boolean;
  login: (usernameOrEmail: string, password: string) => Promise<void>;
  logout: () => void;
  /** Da li trenutni korisnik ima bilo koju od zadatih rola. */
  hasRole: (...codes: string[]) => boolean;
  isAdmin: boolean;
  /** ADMIN ili HR — sme da radi sa tudjim podacima (isto pravilo kao SecurityUtils na backendu). */
  isPrivileged: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  // Ucitavanje traje samo ako uopste postoji sacuvan token koji treba proveriti.
  const [loading, setLoading] = useState(() => !!getStoredToken());
  const queryClient = useQueryClient();

  const logout = useCallback(() => {
    setStoredToken(null);
    setUser(null);
    queryClient.clear();
  }, [queryClient]);

  // Ako backend odbije token (istekao/nevalidan), odjavi korisnika.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      setStoredToken(null);
      setUser(null);
    });
    return () => setUnauthorizedHandler(null);
  }, []);

  // Pocetno ucitavanje: ako postoji token u localStorage, povuci profil.
  useEffect(() => {
    if (!getStoredToken()) return;
    authApi
      .me()
      .then(setUser)
      .catch(() => setStoredToken(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (usernameOrEmail: string, password: string) => {
    const auth = await authApi.login(usernameOrEmail, password);
    setStoredToken(auth.token);
    try {
      setUser(await authApi.me());
    } catch (error) {
      setStoredToken(null);
      throw error;
    }
  }, []);

  const value = useMemo<AuthContextValue>(() => {
    const hasRole = (...codes: string[]) => !!user?.roleCode && codes.includes(user.roleCode);
    return {
      user,
      loading,
      login,
      logout,
      hasRole,
      isAdmin: hasRole('ADMIN'),
      isPrivileged: hasRole('ADMIN', 'HR'),
    };
  }, [user, loading, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth mora biti korišćen unutar <AuthProvider>');
  }
  return context;
}
