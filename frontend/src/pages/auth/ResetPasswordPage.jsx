import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box, Button, TextField, Typography, Alert, CircularProgress,
  Paper, InputAdornment, IconButton,
} from '@mui/material';
import {
  LockOutlined, WaterDropOutlined, CheckCircleOutlined,
  Visibility, VisibilityOff,
} from '@mui/icons-material';
import { useFormError } from '@hooks/useFormError';
import authApi from '@api/authApi';

const schema = z.object({
  newPassword: z
    .string()
    .min(8, 'Password must be at least 8 characters.')
    .regex(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
      'Must contain uppercase, lowercase, digit and special character (@$!%*?&).'
    ),
  confirmPassword: z.string().min(1, 'Please confirm your password.'),
}).refine((d) => d.newPassword === d.confirmPassword, {
  message: 'Passwords do not match.',
  path: ['confirmPassword'],
});

/**
 * Reset Password page — validates the token from the URL, then resets.
 */
function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [success, setSuccess]           = useState(false);
  const [showNew, setShowNew]           = useState(false);
  const [showConfirm, setShowConfirm]   = useState(false);
  const { formError, handleError, clearError } = useFormError();

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async ({ newPassword, confirmPassword }) => {
    if (!token) return;
    setIsSubmitting(true);
    clearError();
    try {
      await authApi.resetPassword({ token, newPassword, confirmPassword });
      setSuccess(true);
    } catch (err) {
      handleError(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const wrapperSx = {
    minHeight: '100vh', display: 'flex', alignItems: 'center',
    justifyContent: 'center', p: 2,
    background: 'linear-gradient(135deg, #0d47a1 0%, #006064 100%)',
  };

  const cardSx = {
    width: '100%', maxWidth: 440, borderRadius: '20px',
    overflow: 'hidden', boxShadow: '0 24px 64px rgba(0,0,0,0.25)',
  };

  // Missing token
  if (!token) {
    return (
      <Box sx={wrapperSx}>
        <Paper sx={{ ...cardSx, p: 5, textAlign: 'center' }}>
          <Alert severity="error" sx={{ mb: 3, borderRadius: '10px' }}>
            Invalid or missing reset token. Please request a new password reset link.
          </Alert>
          <Button variant="contained" fullWidth onClick={() => navigate('/forgot-password')}
                  sx={{ borderRadius: '12px', py: 1.5 }}>
            Request New Link
          </Button>
        </Paper>
      </Box>
    );
  }

  // Success
  if (success) {
    return (
      <Box sx={wrapperSx}>
        <Paper sx={{ ...cardSx, p: 5, textAlign: 'center' }}>
          <CheckCircleOutlined sx={{ fontSize: 72, color: 'success.main', mb: 2 }} />
          <Typography variant="h5" fontWeight={700} gutterBottom>
            Password Reset!
          </Typography>
          <Typography color="text.secondary" mb={4}>
            Your password has been updated successfully. All existing sessions have been
            terminated. Please log in with your new password.
          </Typography>
          <Button variant="contained" fullWidth onClick={() => navigate('/login')}
                  sx={{ borderRadius: '12px', py: 1.5, fontWeight: 700,
                        background: 'linear-gradient(135deg, #1976d2, #00acc1)' }}>
            Go to Login
          </Button>
        </Paper>
      </Box>
    );
  }

  return (
    <Box sx={wrapperSx}>
      <Paper sx={cardSx}>
        <Box sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #00acc1 100%)',
          p: 4, textAlign: 'center',
        }}>
          <WaterDropOutlined sx={{ fontSize: 44, color: '#fff', mb: 1 }} />
          <Typography variant="h5" fontWeight={700} color="white">Set New Password</Typography>
          <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)', mt: 0.5 }}>
            Choose a strong, unique password
          </Typography>
        </Box>

        <Box sx={{ p: 4 }}>
          {formError && (
            <Alert severity="error" sx={{ mb: 2, borderRadius: '10px' }} onClose={clearError}>
              {formError}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <TextField
              {...register('newPassword')}
              label="New Password"
              type={showNew ? 'text' : 'password'}
              fullWidth autoFocus
              error={!!errors.newPassword}
              helperText={errors.newPassword?.message || 'Min 8 chars: uppercase, lowercase, digit, special char'}
              sx={{ mb: 2 }}
              InputProps={{
                startAdornment: <InputAdornment position="start"><LockOutlined fontSize="small" color="action" /></InputAdornment>,
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowNew(!showNew)} edge="end" size="small">
                      {showNew ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              {...register('confirmPassword')}
              label="Confirm New Password"
              type={showConfirm ? 'text' : 'password'}
              fullWidth
              error={!!errors.confirmPassword}
              helperText={errors.confirmPassword?.message}
              sx={{ mb: 3 }}
              InputProps={{
                startAdornment: <InputAdornment position="start"><LockOutlined fontSize="small" color="action" /></InputAdornment>,
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowConfirm(!showConfirm)} edge="end" size="small">
                      {showConfirm ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button
              type="submit" fullWidth variant="contained" size="large"
              disabled={isSubmitting}
              sx={{ py: 1.5, borderRadius: '12px', fontWeight: 700,
                    background: 'linear-gradient(135deg, #1976d2, #00acc1)' }}
            >
              {isSubmitting ? <CircularProgress size={22} sx={{ color: 'white' }} /> : 'Reset Password'}
            </Button>
          </Box>

          <Box textAlign="center" mt={3}>
            <Link to="/login" style={{ color: '#1976d2', fontSize: 14, textDecoration: 'none' }}>
              ← Back to Login
            </Link>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
}

export default ResetPasswordPage;
