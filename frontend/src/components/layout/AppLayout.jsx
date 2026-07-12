import React, { useState } from 'react';
import { Box, useMediaQuery, useTheme } from '@mui/material';
import Sidebar from './Sidebar';
import Navbar  from './Navbar';

/**
 * Main application layout shell for all protected pages.
 *
 * Renders:
 * - Persistent sidebar on desktop (md+)
 * - Temporary drawer sidebar on mobile
 * - Sticky top navbar
 * - Page content area with scroll
 *
 * Usage in App.jsx:
 *   <ProtectedRoute>
 *     <AppLayout>
 *       <ApartmentsPage />
 *     </AppLayout>
 *   </ProtectedRoute>
 */
function AppLayout({ children }) {
  const muiTheme = useTheme();
  const isMobile = useMediaQuery(muiTheme.breakpoints.down('md'));
  const [drawerOpen, setDrawerOpen] = useState(false);

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: 'background.default' }}>

      {/* Desktop sidebar — permanent */}
      {!isMobile && (
        <Sidebar variant="permanent" />
      )}

      {/* Mobile sidebar — temporary drawer */}
      {isMobile && (
        <Sidebar
          variant="temporary"
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
        />
      )}

      {/* Right column: navbar + content */}
      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0, overflow: 'hidden' }}>

        <Navbar onMenuClick={() => setDrawerOpen(true)} />

        {/* Page content */}
        <Box
          component="main"
          sx={{
            flex: 1,
            p: { xs: 2, sm: 3 },
            overflowY: 'auto',
            overflowX: 'hidden',
          }}
        >
          {children}
        </Box>

      </Box>
    </Box>
  );
}

export default AppLayout;
