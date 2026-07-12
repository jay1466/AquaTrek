import { format, parseISO } from 'date-fns';

/** Format ISO date string for display */
export const formatDate = (dateStr) => {
  if (!dateStr) return '—';
  try { return format(parseISO(dateStr), 'dd MMM yyyy'); }
  catch { return dateStr; }
};

export const formatDateTime = (dateStr) => {
  if (!dateStr) return '—';
  try { return format(parseISO(dateStr), 'dd MMM yyyy, HH:mm'); }
  catch { return dateStr; }
};

/** Extract API error message */
export const getErrorMessage = (error, fallback = 'Something went wrong.') => {
  return error?.response?.data?.message
      || error?.response?.data?.validationErrors
         ? Object.values(error.response.data.validationErrors)[0]
         : error?.message
      || fallback;
};

/** Capitalise first letter */
export const capitalize = (str) =>
  str ? str.charAt(0).toUpperCase() + str.slice(1).toLowerCase() : '';

/** Truncate a string */
export const truncate = (str, maxLen = 50) =>
  str && str.length > maxLen ? str.slice(0, maxLen) + '…' : str;

/** Build query params string */
export const buildQuery = (params) =>
  Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
    .join('&');
