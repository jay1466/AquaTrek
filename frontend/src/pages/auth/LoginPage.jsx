import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box, Button, TextField, Typography, InputAdornment,
  IconButton, Alert, CircularProgress, Divider, Paper,
} from '@mui/material';
import {
  Visibility, VisibilityOff, EmailOutlined, LockOutlined,
  WaterDropOutlined,
} from '@mui/icons-material';
import { useAuth } from '@context/AuthContext';
import { useFormError } from '@hooks/useFormError';

// ── Validation schema ──────────────────────────────────────────
const loginSchema = z.object({
  email:    z.string().min(1, 'Email is required.').email('Please enter a valid email address.'),
  password: z.string().min(1, 'Password is required.'),
});

/**
 * Login page for AquaTrack.
 *
 * Features:
 * - React Hook Form + Zod validation
 * - Show/hide password toggle
 * - Session-expired / insufficient-permissions banners from location state
 * - Redirects to intended destination after login (or /dashboard)
 * - Loading state on the submit button
 */
function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate  = useNavigate();
  const location  = useLocation();
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { formError, handleError, clearError } = useFormError();

  // Determine where to redirect after login
  const from = location.state?.from?.pathname || '/dashboard';

  // If already authenticated, redirect immediately
  useEffect(() => {
    if (isAuthenticated) navigate(from, { replace: true });
  }, [isAuthenticated, navigate, from]);

  const {
    register, handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(loginSchema) });

  const onSubmit = async (data) => {
    setIsSubmitting(true);
    clearError();
    try {
      await login(data);
      navigate(from, { replace: true });
    } catch (err) {
      handleError(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Banner reason from location state
  const reason = location.state?.reason;

  return (
    <Box
      sx={{
        minHeight:       '100vh',
        display:         'flex',
        background:      'linear-gradient(135deg, #0d47a1 0%, #006064 100%)',
        alignItems:      'center',
        justifyContent:  'center',
        p:               2,
      }}
    >
      <Paper
        elevation={0}
        sx={{
          width:        '100%',
          maxWidth:     440,
          borderRadius: '20px',
          overflow:     'hidden',
          boxShadow:    '0 24px 64px rgba(0,0,0,0.25)',
        }}
      >
        {/* ── Header ────────────────────────────────────────── */}
        <Box
          sx={{
            background: 'linear-gradient(135deg, #1976d2 0%, #00acc1 100%)',
            p:          4,
            textAlign:  'center',
          }}
        >
          <WaterDropOutlined sx={{ fontSize: 48, color: '#fff', mb: 1 }} />
          <Typography variant="h5" fontWeight={700} color="white" lineHeight={1}>
            AquaTrack
          </Typography>
          <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)', mt: 0.5 }}>
            Water Management Platform
          </Typography>
        </Box>

        {/* ── Form ──────────────────────────────────────────── */}
        <Box sx={{ p: 4 }}>
          <Typography variant="h6" fontWeight={700} gutterBottom>
            Sign In
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={3}>
            Enter your credentials to access your dashboard.
          </Typography>

          {/* Session banners */}
          {reason === 'session_expired' && (
            <Alert severity="warning" sx={{ mb: 2, borderRadius: '10px' }}>
              Your session has expired. Please sign in again.
            </Alert>
          )}
          {reason === 'auth_required' && (
            <Alert severity="info" sx={{ mb: 2, borderRadius: '10px' }}>
              Please sign in to access that page.
            </Alert>
          )}

          {/* API error */}
          {formError && (
            <Alert severity="error" sx={{ mb: 2, borderRadius: '10px' }} onClose={clearError}>
              {formError}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            {/* Email */}
            <TextField
              {...register('email')}
              label="Email Address"
              type="email"
              fullWidth
              autoComplete="email"
              autoFocus
              error={!!errors.email}
              helperText={errors.email?.message}
              sx={{ mb: 2 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <EmailOutlined fontSize="small" color="action" />
                  </InputAdornment>
                ),
              }}
            />

            {/* Password */}
            <TextField
              {...register('password')}
              label="Password"
              type={showPassword ? 'text' : 'password'}
              fullWidth
              autoComplete="current-password"
              error={!!errors.password}
              helperText={errors.password?.message}
              sx={{ mb: 1 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LockOutlined fontSize="small" color="action" />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                      size="small"
                      aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                      {showPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            {/* Forgot password link */}
            <Box textAlign="right" mb={3}>
              <Link
                to="/forgot-password"
                style={{ fontSize: 13, color: '#1976d2', textDecoration: 'none' }}
              >
                Forgot your password?
              </Link>
            </Box>

            {/* Submit */}
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={isSubmitting}
              sx={{
                py:           1.5,
                borderRadius: '12px',
                fontWeight:   700,
                fontSize:     15,
                background:   'linear-gradient(135deg, #1976d2, #00acc1)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #1565c0, #00838f)',
                },
              }}
            >
              {isSubmitting ? (
                <CircularProgress size={22} sx={{ color: 'white' }} />
              ) : (
                'Sign In'
              )}
            </Button>
          </Box>

          <Divider sx={{ my: 3 }} />

          <Typography variant="body2" textAlign="center" color="text.secondary">
            Don&apos;t have an account?{' '}
            <Link
              to="/register"
              style={{ color: '#1976d2', fontWeight: 600, textDecoration: 'none' }}
            >
              Create one
            </Link>
          </Typography>
        </Box>
      </Paper>
    </Box>
  );
}

export default LoginPage;
