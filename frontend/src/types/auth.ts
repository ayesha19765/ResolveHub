export type UserRole = 'ADMIN' | 'AGENT' | 'REPORTER';

export interface AuthUser {
  email: string;
  role: UserRole;
  name?: string;
  id?: number;
}

export interface AuthCredentials {
  email: string;
  password?: string;
  basicHeader: string;
}

export interface AuthState {
  user: AuthUser | null;
  credentials: AuthCredentials | null;
  isAuthenticated: boolean;
}
