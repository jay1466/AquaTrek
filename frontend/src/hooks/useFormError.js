import { useState, useCallback } from 'react';

/**
 * Extracts a user-friendly error message from an Axios error response.
 * Handles the AquaTrack ApiResponse error envelope format.
 *
 * @param {unknown} error  the caught error object
 * @param {string}  fallback  default message if extraction fails
 * @returns {string}  the error message to display to the user
 */
export function extractErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  if (error?.response?.data?.validationErrors) {
    const errors = error.response.data.validationErrors;
    return Object.values(errors)[0] || fallback;
  }
  if (error?.message) {
    return error.message;
  }
  return fallback;
}

/**
 * Hook to manage form-level error state in auth forms.
 *
 * Provides:
 * - `formError`    — current error string (null if no error)
 * - `setFormError` — manually set error
 * - `clearError`   — clear the current error
 * - `handleError`  — extract and set error from a caught Axios error
 *
 * Usage:
 *   const { formError, handleError, clearError } = useFormError();
 *   try { await login(data); } catch (err) { handleError(err); }
 */
export function useFormError(fallbackMessage) {
  const [formError, setFormError] = useState(null);

  const clearError = useCallback(() => setFormError(null), []);

  const handleError = useCallback((error) => {
    setFormError(extractErrorMessage(error, fallbackMessage));
  }, [fallbackMessage]);

  return { formError, setFormError, clearError, handleError };
}
