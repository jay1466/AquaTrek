import axios from 'axios';

/**
 * AquaTrack Axios Configuration.
 *
 * Creates a configured Axios instance with:
 * 1. Base URL from environment variable
 * 2. Default headers (Content-Type: application/json)
 * 3. Request interceptor — attaches JWT access token from localStorage
 * 4. Response interceptor — handles 401 by attempting silent token refresh,
 *    then redirecting to login if refresh also fails
 *
 * All API calls in the application MUST use this instance, not the default axios.
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Keys used to store tokens in localStorage
const ACCESS_TOKEN_KEY  = 'aquatrack_access_token';
const REFRESH_TOKEN_KEY = 'aquatrack_refresh_token';

// ── Main Axios Instance ───────────────────────────────────────
const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,   // 30 second timeout
  headers: {
    'Content-Type': 'application/json',
    'Accept':       'application/json',
  },
});

// ── Request Interceptor ──────────────────────────────────────
// Attaches the current JWT access token to every request
apiClient.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Track whether a token refresh is already in progress to
// avoid sending multiple refresh requests simultaneously
let isRefreshing = false;

// Queue of requests that arrived while a refresh was in progress
let failedQueue = [];

/** Resolves or rejects all queued requests after a refresh attempt. */
const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// ── Response Interceptor ─────────────────────────────────────
apiClient.interceptors.response.use(
  // Pass through successful responses unchanged
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    // Only attempt refresh on 401 Unauthorized, not on retry or refresh endpoint itself
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/auth/refresh')
    ) {
      if (isRefreshing) {
        // Queue this request until the refresh completes
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((newToken) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

      if (!refreshToken) {
        // No refresh token — force logout
        clearAuthAndRedirect();
        return Promise.reject(error);
      }

      try {
        // Attempt to get a new access token
        const { data } = await axios.post(`${BASE_URL}/api/v1/auth/refresh`, {
          refreshToken,
        });

        const newAccessToken = data.data?.accessToken;

        if (!newAccessToken) {
          throw new Error('Refresh response did not include an access token.');
        }

        // Persist the new tokens
        localStorage.setItem(ACCESS_TOKEN_KEY, newAccessToken);
        if (data.data?.refreshToken) {
          localStorage.setItem(REFRESH_TOKEN_KEY, data.data.refreshToken);
        }

        // Update the default header and retry the original request
        apiClient.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

        processQueue(null, newAccessToken);
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed — clear auth and redirect to login
        processQueue(refreshError, null);
        clearAuthAndRedirect();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

/**
 * Clears all stored auth data and redirects the user to the login page.
 * Called when a token refresh fails or a 401 cannot be recovered.
 */
function clearAuthAndRedirect() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem('aquatrack_user');
  // Use window.location to force a full reload and clear React state
  window.location.href = '/login?reason=session_expired';
}

// ── Token Storage Helpers ─────────────────────────────────────
export const tokenStorage = {
  setTokens: (accessToken, refreshToken) => {
    localStorage.setItem(ACCESS_TOKEN_KEY,  accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  },
  getAccessToken:  () => localStorage.getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => localStorage.getItem(REFRESH_TOKEN_KEY),
  clearTokens: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem('aquatrack_user');
  },
};

export default apiClient;
