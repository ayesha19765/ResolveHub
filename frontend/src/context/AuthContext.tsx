import React, { createContext, useContext, useState, useEffect } from 'react';
import { AuthState, AuthUser, UserRole } from '../types/auth';
import { setAuthHeaderGetter, request } from '../api/client';

interface AuthContextType extends AuthState {
  login: (email: string, password?: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const USER_METADATA_MAP: Record<string, { role: UserRole; name: string; id: number }> = {
  'admin@resolvehub.com': { role: 'ADMIN', name: 'Admin User', id: 1 },
  'agent@resolvehub.com': { role: 'AGENT', name: 'Support Agent', id: 2 },
  'reporter@resolvehub.com': { role: 'REPORTER', name: 'Reporter User', id: 3 },
};

const inferRoleFromEmail = (email: string): UserRole => {
  const lower = email.toLowerCase();
  if (lower.includes('admin')) return 'ADMIN';
  if (lower.includes('agent')) return 'AGENT';
  return 'REPORTER';
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [authState, setAuthState] = useState<AuthState>(() => {
    const saved = sessionStorage.getItem('resolvehub_auth');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        return {
          user: parsed.user,
          credentials: { email: parsed.user.email, basicHeader: parsed.basicHeader },
          isAuthenticated: true,
        };
      } catch {
        // invalid cache
      }
    }
    return {
      user: null,
      credentials: null,
      isAuthenticated: false,
    };
  });

  useEffect(() => {
    setAuthHeaderGetter(() => authState.credentials?.basicHeader || null);
  }, [authState]);

  const login = async (email: string, password = '') => {
    const trimmedEmail = email.trim();
    const encoded = btoa(`${trimmedEmail}:${password}`);
    const basicHeader = `Basic ${encoded}`;

    // Test credentials against backend API
    await request('/api/tickets?page=0&size=1', {
      headers: { Authorization: basicHeader },
    });

    const meta = USER_METADATA_MAP[trimmedEmail.toLowerCase()];
    const user: AuthUser = {
      email: trimmedEmail,
      role: meta ? meta.role : inferRoleFromEmail(trimmedEmail),
      name: meta ? meta.name : trimmedEmail.split('@')[0],
      id: meta ? meta.id : 1,
    };

    const newAuthState: AuthState = {
      user,
      credentials: { email: trimmedEmail, basicHeader },
      isAuthenticated: true,
    };

    sessionStorage.setItem(
      'resolvehub_auth',
      JSON.stringify({ user, basicHeader })
    );

    setAuthState(newAuthState);
  };

  const logout = () => {
    sessionStorage.removeItem('resolvehub_auth');
    setAuthState({
      user: null,
      credentials: null,
      isAuthenticated: false,
    });
  };

  return (
    <AuthContext.Provider value={{ ...authState, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
