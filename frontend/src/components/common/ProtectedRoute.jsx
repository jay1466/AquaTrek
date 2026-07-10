import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@context/AuthContext';
import FullPageLoader from '@components/common/FullPageLoader';

/**
 * Route guard component that protects authenticated-only routes.
 *
 * Behaviour:
 * - While session is restoring (isLoading=true) → shows a spinner
 * - If not authenticated → redirects to /login, preserving the
 *   intended destination in location state so the user can be
 *   redirected back after logging in
 * - If authenticated and role check passes → renders children
 * - If authenticated but role check fails → redirects to /dashboard
 *   with an "Unauthorized" message in location state
 *
 * Usage:
 *   <ProtectedRoute>
 *     <DashboardPage />
 *   </ProtectedRoute>
 *
 *   <ProtectedRoute allowedRoles={['ADMIN']}>
 *     <ApartmentsPage />
 *   </ProtectedRoute>
 */
function ProtectedRoute({ children, allowedRoles = [] }) {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  // Show spinner while the auth context is restoring from localStorage/API
  if (isLoading) {
    return <FullPageLoader message="Restoring your session..." />;
  }

  // Not authenticated — redirect to login preserving the intended URL
  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        state={{ from: location, reason: 'auth_required' }}
        replace
      />
    );
  }

  // Role check — if allowedRoles is specified and user's role isn't in the list
  if (allowedRoles.length > 0 && user && !allowedRoles.includes(user.role)) {
    return (
      <Navigate
        to="/dashboard"
        state={{ reason: 'insufficient_permissions' }}
        replace
      />
    );
  }

  return children;
}

export default ProtectedRoute;
