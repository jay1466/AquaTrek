import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box, Typography } from '@mui/material';

import { AuthProvider }  from '@context/AuthContext';
import ProtectedRoute    from '@components/common/ProtectedRoute';
import FullPageLoader    from '@components/common/FullPageLoader';
import PageNotFound      from '@components/common/PageNotFound';

// Auth pages — eager loaded (small, always needed)
import LoginPage          from '@pages/auth/LoginPage';
import RegisterPage       from '@pages/auth/RegisterPage';
import ForgotPasswordPage from '@pages/auth/ForgotPasswordPage';
import ResetPasswordPage  from '@pages/auth/ResetPasswordPage';
import VerifyEmailPage    from '@pages/auth/VerifyEmailPage';

function App() {
  return (
    <AuthProvider>
      <Suspense fallback={<FullPageLoader />}>
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* Public auth routes */}
          <Route path="/login"           element={<LoginPage />} />
          <Route path="/register"        element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password"  element={<ResetPasswordPage />} />
          <Route path="/verify-email"    element={<VerifyEmailPage />} />

          {/* Protected routes */}
          <Route path="/dashboard" element={
            <ProtectedRoute><ComingSoon page="Dashboard" module={2} /></ProtectedRoute>
          } />
          <Route path="/apartments/*" element={
            <ProtectedRoute allowedRoles={['ADMIN','SUPER_ADMIN']}>
              <ComingSoon page="Apartment Management" module={2} />
            </ProtectedRoute>
          } />
          <Route path="/buildings/*" element={
            <ProtectedRoute allowedRoles={['ADMIN','SUPER_ADMIN','MANAGER']}>
              <ComingSoon page="Building Management" module={2} />
            </ProtectedRoute>
          } />
          <Route path="/households/*" element={
            <ProtectedRoute><ComingSoon page="Household Management" module={3} /></ProtectedRoute>
          } />
          <Route path="/meters/*" element={
            <ProtectedRoute><ComingSoon page="Water Meters" module={4} /></ProtectedRoute>
          } />
          <Route path="/readings/*" element={
            <ProtectedRoute><ComingSoon page="Meter Readings" module={4} /></ProtectedRoute>
          } />
          <Route path="/billing/*" element={
            <ProtectedRoute allowedRoles={['ADMIN','SUPER_ADMIN']}>
              <ComingSoon page="Billing Engine" module={5} />
            </ProtectedRoute>
          } />
          <Route path="/invoices/*" element={
            <ProtectedRoute><ComingSoon page="Invoices" module={7} /></ProtectedRoute>
          } />
          <Route path="/payments/*" element={
            <ProtectedRoute><ComingSoon page="Payments" module={7} /></ProtectedRoute>
          } />
          <Route path="/bulk-water/*" element={
            <ProtectedRoute allowedRoles={['ADMIN','SUPER_ADMIN']}>
              <ComingSoon page="Bulk Water Purchase" module={6} />
            </ProtectedRoute>
          } />
          <Route path="/alerts/*" element={
            <ProtectedRoute><ComingSoon page="Alert Management" module={8} /></ProtectedRoute>
          } />
          <Route path="/analytics/*" element={
            <ProtectedRoute><ComingSoon page="Analytics" module={9} /></ProtectedRoute>
          } />
          <Route path="/profile" element={
            <ProtectedRoute><ComingSoon page="My Profile" module={2} /></ProtectedRoute>
          } />
          <Route path="/settings/*" element={
            <ProtectedRoute allowedRoles={['ADMIN','SUPER_ADMIN']}>
              <ComingSoon page="Settings" module={2} />
            </ProtectedRoute>
          } />

          <Route path="*" element={<PageNotFound />} />
        </Routes>
      </Suspense>
    </AuthProvider>
  );
}

function ComingSoon({ page, module: moduleNum }) {
  return (
    <Box display="flex" flexDirection="column" alignItems="center"
         justifyContent="center" minHeight="100vh" gap={2}
         sx={{ backgroundColor: 'background.default' }}>
      <Box sx={{
        width: 64, height: 64, borderRadius: '16px',
        background: 'linear-gradient(135deg, #1976d2, #00acc1)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: '28px', mb: 1,
      }}>💧</Box>
      <Typography variant="h5" fontWeight={700} color="text.primary">AquaTrack</Typography>
      <Typography color="text.secondary">
        <strong>{page}</strong> — Module {moduleNum} (coming soon)
      </Typography>
    </Box>
  );
}

export default App;
