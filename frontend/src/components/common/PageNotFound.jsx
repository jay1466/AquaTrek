import React from 'react';
import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import HomeRoundedIcon from '@mui/icons-material/HomeRounded';

/**
 * 404 Not Found page.
 *
 * Displayed when the user navigates to a URL that doesn't match any route.
 * Provides a button to navigate back to the dashboard.
 */
function PageNotFound() {
  const navigate = useNavigate();

  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      minHeight="100vh"
      gap={2}
      sx={{
        backgroundColor: 'background.default',
        padding: 4,
        textAlign: 'center',
      }}
    >
      {/* Large 404 number */}
      <Typography
        sx={{
          fontSize: { xs: '6rem', sm: '9rem' },
          fontWeight: 800,
          lineHeight: 1,
          background: 'linear-gradient(135deg, #1976d2, #00acc1)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          backgroundClip: 'text',
          mb: 1,
        }}
      >
        404
      </Typography>

      <Typography variant="h4" fontWeight={700} color="text.primary" gutterBottom>
        Page Not Found
      </Typography>

      <Typography
        variant="body1"
        color="text.secondary"
        maxWidth={400}
        mb={4}
      >
        The page you're looking for doesn't exist or has been moved.
        Double-check the URL or navigate back to the dashboard.
      </Typography>

      <Button
        variant="contained"
        size="large"
        startIcon={<HomeRoundedIcon />}
        onClick={() => navigate('/dashboard', { replace: true })}
        sx={{ borderRadius: '12px', px: 4, py: 1.5 }}
      >
        Back to Dashboard
      </Button>
    </Box>
  );
}

export default PageNotFound;
