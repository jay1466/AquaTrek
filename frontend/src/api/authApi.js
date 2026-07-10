import apiClient from './axiosConfig';

const AUTH_BASE = '/api/v1/auth';

/**
 * AquaTrack Authentication API service.
 * All functions return the Axios response data directly.
 */
const authApi = {

  /** Register a new user account */
  register: (data) =>
    apiClient.post(`${AUTH_BASE}/register`, data).then(r => r.data),

  /** Login with email + password — returns tokens + user */
  login: (data) =>
    apiClient.post(`${AUTH_BASE}/login`, data).then(r => r.data),

  /** Refresh the access token using the refresh token */
  refreshToken: (refreshToken) =>
    apiClient.post(`${AUTH_BASE}/refresh`, { refreshToken }).then(r => r.data),

  /** Logout — blacklists current access token */
  logout: () =>
    apiClient.post(`${AUTH_BASE}/logout`).then(r => r.data),

  /** Verify email using the token from the verification link */
  verifyEmail: (token) =>
    apiClient.get(`${AUTH_BASE}/verify-email`, { params: { token } }).then(r => r.data),

  /** Resend email verification link */
  resendVerification: (email) =>
    apiClient.post(`${AUTH_BASE}/resend-verification`, null, { params: { email } }).then(r => r.data),

  /** Initiate forgot password flow */
  forgotPassword: (email) =>
    apiClient.post(`${AUTH_BASE}/forgot-password`, { email }).then(r => r.data),

  /** Reset password using one-time token */
  resetPassword: (data) =>
    apiClient.post(`${AUTH_BASE}/reset-password`, data).then(r => r.data),

  /** Change password for authenticated user */
  changePassword: (data) =>
    apiClient.post(`${AUTH_BASE}/change-password`, data).then(r => r.data),

  /** Get the currently authenticated user's profile */
  getCurrentUser: () =>
    apiClient.get(`${AUTH_BASE}/me`).then(r => r.data),
};

export default authApi;
