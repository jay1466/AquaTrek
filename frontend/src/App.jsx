import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box, Typography } from '@mui/material';

import { AuthProvider }  from '@context/AuthContext';
import ProtectedRoute    from '@components/common/ProtectedRoute';
import AppLayout         from '@components/layout/AppLayout';
import FullPageLoader    from '@components/common/FullPageLoader';
import PageNotFound      from '@components/common/PageNotFound';

// Auth pages — eagerly loaded
import LoginPage          from '@pages/auth/LoginPage';
import RegisterPage       from '@pages/auth/RegisterPage';
import ForgotPasswordPage from '@pages/auth/ForgotPasswordPage';
import ResetPasswordPage  from '@pages/auth/ResetPasswordPage';
import VerifyEmailPage    from '@pages/auth/VerifyEmailPage';

// Protected pages — eagerly loaded (could be lazy for large apps)
import DashboardPage   from '@pages/dashboard/DashboardPage';
import ApartmentsPage  from '@pages/apartments/ApartmentsPage';
import BuildingsPage   from '@pages/buildings/BuildingsPage';

/** Wraps a page in ProtectedRoute + AppLayout */
function Protected({ children, roles = [] }) {
  return (
    <ProtectedRoute allowedRoles={roles}>
      <AppLayout>{children}</AppLayout>
    </ProtectedRoute>
  );
}

/** Placeholder for pages not yet implemented */
function Soon({ page }) {
  return (
    <Box display="flex" flexDirection="column" alignItems="center"
         justifyContent="center" minHeight="60vh" gap={1.5}>
      <Typography variant="h5" fontWeight={700}>💧 {page}</Typography>
      <Typography color="text.secondary">Coming in the next module</Typography>
    </Box>
  );
}

function App() {
  return (
    <AuthProvider>
      <Suspense fallback={<FullPageLoader />}>
        <Routes>

          {/* Root redirect */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* Public auth routes */}
          <Route path="/login"           element={<LoginPage />} />
          <Route path="/register"        element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password"  element={<ResetPasswordPage />} />
          <Route path="/verify-email"    element={<VerifyEmailPage />} />

          {/* Protected — all roles */}
          <Route path="/dashboard"  element={<Protected><DashboardPage /></Protected>} />
          <Route path="/invoices/*" element={<Protected><Soon page="Invoices" /></Protected>} />
          <Route path="/payments/*" element={<Protected><Soon page="Payments" /></Protected>} />
          <Route path="/alerts/*"   element={<Protected><Soon page="Alerts" /></Protected>} />
          <Route path="/profile"    element={<Protected><Soon page="My Profile" /></Protected>} />

          {/* Protected — Admin + */}
          <Route path="/apartments/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN']}>
              <ApartmentsPage />
            </Protected>
          } />

          <Route path="/buildings/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN','MANAGER']}>
              <BuildingsPage />
            </Protected>
          } />

          <Route path="/households/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN','MANAGER']}>
              <Soon page="Households" />
            </Protected>
          } />

          <Route path="/meters/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN','MANAGER']}>
              <Soon page="Water Meters" />
            </Protected>
          } />

          <Route path="/readings/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN','MANAGER']}>
              <Soon page="Meter Readings" />
            </Protected>
          } />

          <Route path="/billing/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN']}>
              <Soon page="Billing Engine" />
            </Protected>
          } />

          <Route path="/bulk-water/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN']}>
              <Soon page="Bulk Water" />
            </Protected>
          } />

          <Route path="/analytics/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN','MANAGER']}>
              <Soon page="Analytics" />
            </Protected>
          } />

          <Route path="/settings/*" element={
            <Protected roles={['ADMIN','SUPER_ADMIN']}>
              <Soon page="Settings" />
            </Protected>
          } />

          {/* 404 */}
          <Route path="*" element={<PageNotFound />} />

        </Routes>
      </Suspense>
    </AuthProvider>
  );
}

export default App;
