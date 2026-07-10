import React, {
  createContext, useCallback, useContext,
  useEffect, useMemo, useReducer,
} from 'react';
import { useNavigate } from 'react-router-dom';
import { tokenStorage } from '@api/axiosConfig';
import authApi from '@api/authApi';

// ── State shape ───────────────────────────────────────────────
const initialState = {
  user:          null,       // UserSummary from AuthResponse
  isAuthenticated: false,
  isLoading:     true,       // true while validating existing session on mount
};

// ── Reducer ───────────────────────────────────────────────────
function authReducer(state, action) {
  switch (action.type) {
    case 'AUTH_INIT_START':
      return { ...state, isLoading: true };
    case 'AUTH_SUCCESS':
      return { user: action.payload, isAuthenticated: true, isLoading: false };
    case 'AUTH_LOGOUT':
      return { user: null, isAuthenticated: false, isLoading: false };
    case 'AUTH_INIT_DONE':
      return { ...state, isLoading: false };
    default:
      return state;
  }
}

// ── Context ───────────────────────────────────────────────────
const AuthContext = createContext(null);

/**
 * AuthProvider — wraps the app with authentication state.
 *
 * On mount, it attempts to restore the session by calling GET /auth/me
 * using the stored access token. If that fails (expired / blacklisted),
 * the user is treated as logged out.
 *
 * Provides:
 * - `user`            — the authenticated user object (or null)
 * - `isAuthenticated` — boolean
 * - `isLoading`       — true during session restore on mount
 * - `login(data)`     — async; stores tokens, sets user
 * - `logout()`        — clears tokens, redirects to login
 * - `register(data)`  — async; returns message
 */
export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, initialState);
  const navigate = useNavigate();

  // ── Session restore on mount ──────────────────────────────
  useEffect(() => {
    const restoreSession = async () => {
      const token = tokenStorage.getAccessToken();
      if (!token) {
        dispatch({ type: 'AUTH_INIT_DONE' });
        return;
      }
      try {
        const response = await authApi.getCurrentUser();
        if (response.success && response.data) {
          dispatch({ type: 'AUTH_SUCCESS', payload: response.data });
        } else {
          tokenStorage.clearTokens();
          dispatch({ type: 'AUTH_LOGOUT' });
        }
      } catch {
        // Token expired or blacklisted — clear silently
        tokenStorage.clearTokens();
        dispatch({ type: 'AUTH_LOGOUT' });
      }
    };
    restoreSession();
  }, []);

  // ── Login ────────────────────────────────────────────────
  const login = useCallback(async (credentials) => {
    const response = await authApi.login(credentials);
    if (response.success && response.data) {
      const { accessToken, refreshToken, user } = response.data;
      tokenStorage.setTokens(accessToken, refreshToken);
      localStorage.setItem('aquatrack_user', JSON.stringify(user));
      dispatch({ type: 'AUTH_SUCCESS', payload: user });
      return response;
    }
    throw new Error(response.message || 'Login failed.');
  }, []);

  // ── Logout ───────────────────────────────────────────────
  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // Ignore logout API errors — clear locally regardless
    } finally {
      tokenStorage.clearTokens();
      dispatch({ type: 'AUTH_LOGOUT' });
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  // ── Register ─────────────────────────────────────────────
  const register = useCallback(async (data) => {
    const response = await authApi.register(data);
    return response;
  }, []);

  // ── Role helpers ──────────────────────────────────────────
  const hasRole = useCallback((role) => {
    return state.user?.role === role;
  }, [state.user]);

  const isAdmin = useMemo(() =>
    state.user?.role === 'ADMIN' || state.user?.role === 'SUPER_ADMIN',
  [state.user]);

  const isResident = useMemo(() =>
    state.user?.role === 'RESIDENT',
  [state.user]);

  const contextValue = useMemo(() => ({
    ...state,
    login,
    logout,
    register,
    hasRole,
    isAdmin,
    isResident,
  }), [state, login, logout, register, hasRole, isAdmin, isResident]);

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Hook for consuming the auth context.
 * Must be used inside <AuthProvider>.
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export default AuthContext;
