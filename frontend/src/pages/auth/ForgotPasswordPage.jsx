import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box, Button, TextField, Typography, Alert, CircularProgress,
  Paper, InputAdornment,
} from '@mui/material';
import {
  EmailOutlined, WaterDropOutlined, MarkEmailReadOutlined,
  ArrowBackOutlined,
} from '@mui/icons-material';
import { useFormError } from '@hooks/useFormError';
import authApi from '@api/authApi';

const schema = z.object({
  email: z.string().min(1, 'Email is required.').email('Please enter a valid email address.'),
});

/**
 * Forgot Password page — submits the email and shows a success state.
 */
function ForgotPasswordPage() {
  const [isSubmitting, setIsSubmitting]  = useState(false);
  const [submitted, setSubmitted]        = useState(false);
  const [submittedEmail, setSubmittedEmail] = useState('');
  const { formError, handleError, clearError } = useFormError();

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async ({ email }) => {
    setIsSubmitting(true);
    clearError();
    try {
      await authApi.forgotPassword(email);
      setSubmittedEmail(email);
      setSubmitted(true);
    } catch (err) {
      handleError(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const cardSx = {
    width: '100%', maxWidth: 440, borderRadius: '20px',
    overflow: 'hidden', boxShadow: '0 24px 64px rgba(0,0,0,0.25)',
  };

  const wrapperSx = {
    minHeight: '100vh', display: 'flex', alignItems: 'center',
    justifyContent: 'center', p: 2,
    background: 'linear-gradient(135deg, #0d47a1 0%, #006064 100%)',
  };

  // ── Success State ──────────────────────────────────────────
  if (submitted) {
    return (
      <Box sx={wrapperSx}>
        <Paper sx={{ ...cardSx, p: 5, textAlign: 'center' }}>
          <MarkEmailReadOutlined sx={{ fontSize: 72, color: 'primary.main', mb: 2 }} />
          <Typography variant="h5" fontWeight={700} gutterBottom>
            Check Your Email
          </Typography>
          <Typography color="text.secondary" mb={1}>
            If an account exists for
          </Typography>
          <Typography fontWeight={600} color="primary.main" mb={3}>
            {submittedEmail}
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={4}>
            you'll receive a password reset link shortly.
            The link expires in 24 hours. Check your spam folder if you don't see it.
          </Typography>
          <Button
            variant="outlined"
            fullWidth
            onClick={() => setSubmitted(false)}
            sx={{ mb: 2, borderRadius: '12px', py: 1.5 }}
          >
            Try a Different Email
          </Button>
          <Link to="/login" style={{ color: '#1976d2', fontSize: 14, textDecoration: 'none' }}>
            ← Back to Login
          </Link>
        </Paper>
      </Box>
    );
  }

  // ── Form State ─────────────────────────────────────────────
  return (
    <Box sx={wrapperSx}>
      <Paper sx={cardSx}>
        {/* Header */}
        <Box sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #00acc1 100%)',
          p: 4, textAlign: 'center',
        }}>
          <WaterDropOutlined sx={{ fontSize: 44, color: '#fff', mb: 1 }} />
          <Typography variant="h5" fontWeight={700} color="white">
            Forgot Password?
          </Typography>
          <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)', mt: 0.5 }}>
            We'll send you a reset link
          </Typography>
        </Box>

        {/* Form */}
        <Box sx={{ p: 4 }}>
          <Typography variant="body2" color="text.secondary" mb={3}>
            Enter the email address associated with your account and we'll email you
            a link to reset your password.
          </Typography>

          {formError && (
            <Alert severity="error" sx={{ mb: 2, borderRadius: '10px' }} onClose={clearError}>
              {formError}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            <TextField
              {...register('email')}
              label="Email Address"
              type="email"
              fullWidth
              autoFocus
              autoComplete="email"
              error={!!errors.email}
              helperText={errors.email?.message}
              sx={{ mb: 3 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <EmailOutlined fontSize="small" color="action" />
                  </InputAdornment>
                ),
              }}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={isSubmitting}
              sx={{
                py: 1.5, borderRadius: '12px', fontWeight: 700,
                background: 'linear-gradient(135deg, #1976d2, #00acc1)',
              }}
            >
              {isSubmitting
                ? <CircularProgress size={22} sx={{ color: 'white' }} />
                : 'Send Reset Link'}
            </Button>
          </Box>

          <Box textAlign="center" mt={3}>
            <Link to="/login" style={{ color: '#1976d2', fontSize: 14, fontWeight: 500, textDecoration: 'none' }}>
              <ArrowBackOutlined sx={{ fontSize: 14, mr: 0.5, verticalAlign: 'middle' }} />
              Back to Login
            </Link>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
}

export default ForgotPasswordPage;
