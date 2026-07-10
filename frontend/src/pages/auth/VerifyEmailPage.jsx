import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import {
  Box, Button, Typography, Paper, CircularProgress, Alert,
} from '@mui/material';
import {
  CheckCircleOutlined, ErrorOutlined, WaterDropOutlined,
} from '@mui/icons-material';
import authApi from '@api/authApi';

/**
 * Email Verification page.
 *
 * On mount, reads the `token` query parameter and calls the verify-email API.
 * Displays success or error based on the API response.
 * Automatically redirects to login after 5 seconds on success.
 */
function VerifyEmailPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus]   = useState('loading'); // loading | success | error
  const [message, setMessage] = useState('');
  const [countdown, setCountdown] = useState(5);

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('No verification token found. Please use the link from your email.');
      return;
    }

    const verify = async () => {
      try {
        const response = await authApi.verifyEmail(token);
        setMessage(response.data || 'Email verified successfully!');
        setStatus('success');
      } catch (err) {
        setStatus('error');
        setMessage(
          err?.response?.data?.message ||
          'This verification link is invalid or has expired. Please request a new one.'
        );
      }
    };

    verify();
  }, [token]);

  // Countdown redirect on success
  useEffect(() => {
    if (status !== 'success') return;
    const interval = setInterval(() => {
      setCountdown((c) => {
        if (c <= 1) {
          clearInterval(interval);
          navigate('/login');
          return 0;
        }
        return c - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [status, navigate]);

  const wrapperSx = {
    minHeight: '100vh', display: 'flex', alignItems: 'center',
    justifyContent: 'center', p: 2,
    background: 'linear-gradient(135deg, #0d47a1 0%, #006064 100%)',
  };

  return (
    <Box sx={wrapperSx}>
      <Paper sx={{
        width: '100%', maxWidth: 440, borderRadius: '20px',
        overflow: 'hidden', boxShadow: '0 24px 64px rgba(0,0,0,0.25)',
      }}>
        {/* Header */}
        <Box sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #00acc1 100%)',
          p: 4, textAlign: 'center',
        }}>
          <WaterDropOutlined sx={{ fontSize: 44, color: '#fff', mb: 1 }} />
          <Typography variant="h5" fontWeight={700} color="white">AquaTrack</Typography>
          <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)', mt: 0.5 }}>
            Email Verification
          </Typography>
        </Box>

        {/* Body */}
        <Box sx={{ p: 5, textAlign: 'center' }}>
          {/* Loading */}
          {status === 'loading' && (
            <>
              <CircularProgress size={56} thickness={4} sx={{ mb: 3, color: 'primary.main' }} />
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Verifying your email…
              </Typography>
              <Typography color="text.secondary">
                Please wait a moment.
              </Typography>
            </>
          )}

          {/* Success */}
          {status === 'success' && (
            <>
              <CheckCircleOutlined sx={{ fontSize: 72, color: 'success.main', mb: 2 }} />
              <Typography variant="h5" fontWeight={700} gutterBottom>
                Email Verified!
              </Typography>
              <Typography color="text.secondary" mb={1}>
                {message}
              </Typography>
              <Alert severity="success" sx={{ mb: 3, borderRadius: '10px', textAlign: 'left' }}>
                Your account is now active. Redirecting to login in{' '}
                <strong>{countdown}</strong> second{countdown !== 1 ? 's' : ''}…
              </Alert>
              <Button
                variant="contained"
                fullWidth
                size="large"
                onClick={() => navigate('/login')}
                sx={{
                  borderRadius: '12px', py: 1.5, fontWeight: 700,
                  background: 'linear-gradient(135deg, #1976d2, #00acc1)',
                }}
              >
                Go to Login Now
              </Button>
            </>
          )}

          {/* Error */}
          {status === 'error' && (
            <>
              <ErrorOutlined sx={{ fontSize: 72, color: 'error.main', mb: 2 }} />
              <Typography variant="h5" fontWeight={700} gutterBottom>
                Verification Failed
              </Typography>
              <Alert severity="error" sx={{ mb: 3, borderRadius: '10px', textAlign: 'left' }}>
                {message}
              </Alert>
              <Button
                variant="contained"
                fullWidth
                size="large"
                onClick={() => navigate('/login', {
                  state: { resendEmail: true },
                })}
                sx={{ borderRadius: '12px', py: 1.5, mb: 2, fontWeight: 700 }}
              >
                Request New Verification Email
              </Button>
              <Link to="/login" style={{ color: '#1976d2', fontSize: 14, textDecoration: 'none' }}>
                ← Back to Login
              </Link>
            </>
          )}
        </Box>
      </Paper>
    </Box>
  );
}

export default VerifyEmailPage;
