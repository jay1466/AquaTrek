import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Box, Button, TextField, Typography, InputAdornment, IconButton,
  Alert, CircularProgress, Paper, MenuItem, Select, FormControl,
  FormHelperText, InputLabel, Stepper, Step, StepLabel, Divider,
} from '@mui/material';
import {
  Visibility, VisibilityOff, EmailOutlined, LockOutlined,
  PersonOutlined, PhoneOutlined, WaterDropOutlined, CheckCircleOutlined,
} from '@mui/icons-material';
import { useFormError } from '@hooks/useFormError';
import authApi from '@api/authApi';

// ── Validation schema ──────────────────────────────────────────
const registerSchema = z.object({
  firstName: z
    .string()
    .min(2, 'First name must be at least 2 characters.')
    .max(100, 'First name must be less than 100 characters.')
    .regex(/^[a-zA-Z\s'-]+$/, 'First name may only contain letters, spaces, hyphens, and apostrophes.'),
  lastName: z
    .string()
    .min(2, 'Last name must be at least 2 characters.')
    .max(100, 'Last name must be less than 100 characters.')
    .regex(/^[a-zA-Z\s'-]+$/, 'Last name may only contain letters, spaces, hyphens, and apostrophes.'),
  email: z
    .string()
    .min(1, 'Email is required.')
    .email('Please enter a valid email address.'),
  password: z
    .string()
    .min(8, 'Password must be at least 8 characters.')
    .regex(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
      'Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&).'
    ),
  confirmPassword: z.string().min(1, 'Please confirm your password.'),
  phoneNumber: z
    .string()
    .regex(/^[+]?[0-9\s\-().]{7,20}$/, 'Please enter a valid phone number.')
    .optional()
    .or(z.literal('')),
  gender: z.string().optional(),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Passwords do not match.',
  path: ['confirmPassword'],
});

const GENDER_OPTIONS = [
  { value: 'MALE',             label: 'Male' },
  { value: 'FEMALE',           label: 'Female' },
  { value: 'OTHER',            label: 'Other' },
  { value: 'PREFER_NOT_TO_SAY', label: 'Prefer not to say' },
];

/**
 * User registration page.
 *
 * Displays a two-step flow:
 * Step 1 — Fill the form
 * Step 2 — Success state (email sent confirmation)
 */
function RegisterPage() {
  const navigate = useNavigate();
  const [showPassword, setShowPassword]        = useState(false);
  const [showConfirm, setShowConfirm]          = useState(false);
  const [isSubmitting, setIsSubmitting]        = useState(false);
  const [registeredEmail, setRegisteredEmail]  = useState('');
  const [success, setSuccess]                  = useState(false);
  const { formError, handleError, clearError } = useFormError();

  const { register, handleSubmit, control, formState: { errors } } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: { gender: '' },
  });

  const onSubmit = async (data) => {
    setIsSubmitting(true);
    clearError();
    try {
      const payload = {
        firstName:   data.firstName.trim(),
        lastName:    data.lastName.trim(),
        email:       data.email.toLowerCase().trim(),
        password:    data.password,
        phoneNumber: data.phoneNumber || undefined,
        gender:      data.gender     || undefined,
      };
      await authApi.register(payload);
      setRegisteredEmail(data.email);
      setSuccess(true);
    } catch (err) {
      handleError(err);
    } finally {
      setIsSubmitting(false);
    }
  };

  // ── Success State ──────────────────────────────────────────
  if (success) {
    return (
      <Box sx={{
        minHeight: '100vh', display: 'flex', alignItems: 'center',
        justifyContent: 'center', p: 2,
        background: 'linear-gradient(135deg, #0d47a1 0%, #006064 100%)',
      }}>
        <Paper sx={{ maxWidth: 480, width: '100%', borderRadius: '20px', p: 5, textAlign: 'center' }}>
          <CheckCircleOutlined sx={{ fontSize: 72, color: 'success.main', mb: 2 }} />
          <Typography variant="h5" fontWeight={700} gutterBottom>
            Check Your Inbox!
          </Typography>
          <Typography variant="body1" color="text.secondary" mb={1}>
            We've sent a verification link to:
          </Typography>
          <Typography variant="body1" fontWeight={600} color="primary.main" mb={3}>
            {registeredEmail}
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={4}>
            Click the link in that email to activate your account.
            The link expires in 48 hours.
          </Typography>
          <Button
            variant="contained"
            fullWidth
            size="large"
            onClick={() => navigate('/login')}
            sx={{ borderRadius: '12px', py: 1.5, fontWeight: 700 }}
          >
            Go to Login
          </Button>
          <Typography variant="body2" color="text.secondary" mt={2}>
            Didn't receive it?{' '}
            <Box
              component="span"
              sx={{ color: 'primary.main', cursor: 'pointer', fontWeight: 600 }}
              onClick={async () => {
                await authApi.resendVerification(registeredEmail);
              }}
            >
              Resend email
            </Box>
          </Typography>
        </Paper>
      </Box>
    );
  }

  // ── Registration Form ──────────────────────────────────────
  return (
    <Box sx={{
      minHeight: '100vh', display: 'flex', alignItems: 'center',
      justifyContent: 'center', p: 2,
      background: 'linear-gradient(135deg, #0d47a1 0%, #006064 100%)',
    }}>
      <Paper sx={{ width: '100%', maxWidth: 520, borderRadius: '20px', overflow: 'hidden',
                   boxShadow: '0 24px 64px rgba(0,0,0,0.25)' }}>

        {/* Header */}
        <Box sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #00acc1 100%)',
          p: 4, textAlign: 'center',
        }}>
          <WaterDropOutlined sx={{ fontSize: 44, color: '#fff', mb: 1 }} />
          <Typography variant="h5" fontWeight={700} color="white">
            Create Your Account
          </Typography>
          <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)', mt: 0.5 }}>
            Join AquaTrack — Water Management Platform
          </Typography>
        </Box>

        {/* Form */}
        <Box sx={{ p: 4 }}>
          {formError && (
            <Alert severity="error" sx={{ mb: 2, borderRadius: '10px' }} onClose={clearError}>
              {formError}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
            {/* Name Row */}
            <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
              <TextField
                {...register('firstName')}
                label="First Name"
                fullWidth
                autoFocus
                error={!!errors.firstName}
                helperText={errors.firstName?.message}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <PersonOutlined fontSize="small" color="action" />
                    </InputAdornment>
                  ),
                }}
              />
              <TextField
                {...register('lastName')}
                label="Last Name"
                fullWidth
                error={!!errors.lastName}
                helperText={errors.lastName?.message}
              />
            </Box>

            {/* Email */}
            <TextField
              {...register('email')}
              label="Email Address"
              type="email"
              fullWidth
              autoComplete="email"
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

            {/* Phone */}
            <TextField
              {...register('phoneNumber')}
              label="Phone Number (optional)"
              fullWidth
              error={!!errors.phoneNumber}
              helperText={errors.phoneNumber?.message}
              sx={{ mb: 2 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <PhoneOutlined fontSize="small" color="action" />
                  </InputAdornment>
                ),
              }}
            />

            {/* Gender */}
            <Controller
              name="gender"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth sx={{ mb: 2 }} error={!!errors.gender}>
                  <InputLabel>Gender (optional)</InputLabel>
                  <Select {...field} label="Gender (optional)">
                    <MenuItem value=""><em>Prefer not to say</em></MenuItem>
                    {GENDER_OPTIONS.map((opt) => (
                      <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                    ))}
                  </Select>
                  {errors.gender && <FormHelperText>{errors.gender.message}</FormHelperText>}
                </FormControl>
              )}
            />

            {/* Password */}
            <TextField
              {...register('password')}
              label="Password"
              type={showPassword ? 'text' : 'password'}
              fullWidth
              autoComplete="new-password"
              error={!!errors.password}
              helperText={errors.password?.message || 'Min 8 chars: uppercase, lowercase, digit, special char'}
              sx={{ mb: 2 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LockOutlined fontSize="small" color="action" />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" size="small">
                      {showPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            {/* Confirm Password */}
            <TextField
              {...register('confirmPassword')}
              label="Confirm Password"
              type={showConfirm ? 'text' : 'password'}
              fullWidth
              autoComplete="new-password"
              error={!!errors.confirmPassword}
              helperText={errors.confirmPassword?.message}
              sx={{ mb: 3 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <LockOutlined fontSize="small" color="action" />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowConfirm(!showConfirm)} edge="end" size="small">
                      {showConfirm ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            {/* Submit */}
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={isSubmitting}
              sx={{
                py: 1.5, borderRadius: '12px', fontWeight: 700, fontSize: 15,
                background: 'linear-gradient(135deg, #1976d2, #00acc1)',
                '&:hover': { background: 'linear-gradient(135deg, #1565c0, #00838f)' },
              }}
            >
              {isSubmitting
                ? <CircularProgress size={22} sx={{ color: 'white' }} />
                : 'Create Account'}
            </Button>
          </Box>

          <Divider sx={{ my: 3 }} />

          <Typography variant="body2" textAlign="center" color="text.secondary">
            Already have an account?{' '}
            <Link to="/login" style={{ color: '#1976d2', fontWeight: 600, textDecoration: 'none' }}>
              Sign in
            </Link>
          </Typography>
        </Box>
      </Paper>
    </Box>
  );
}

export default RegisterPage;
