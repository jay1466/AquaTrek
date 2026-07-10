import React from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';

/**
 * Full-page loading spinner displayed while lazy-loaded route chunks
 * are being fetched (used by React.Suspense fallback in App.jsx).
 *
 * Also used as a blocking overlay during critical operations like
 * initial auth token validation.
 */
function FullPageLoader({ message = 'Loading AquaTrack...' }) {
  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      minHeight="100vh"
      gap={3}
      sx={{ backgroundColor: 'background.default' }}
    >
      {/* Brand logo mark */}
      <Box
        sx={{
          width: 56,
          height: 56,
          borderRadius: '14px',
          background: 'linear-gradient(135deg, #1976d2 0%, #00acc1 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '26px',
          boxShadow: '0 8px 24px rgba(25, 118, 210, 0.35)',
        }}
      >
        💧
      </Box>

      <CircularProgress
        size={36}
        thickness={4}
        sx={{ color: 'primary.main' }}
      />

      <Typography
        variant="body2"
        color="text.secondary"
        sx={{ letterSpacing: '0.02em' }}
      >
        {message}
      </Typography>
    </Box>
  );
}

export default FullPageLoader;
