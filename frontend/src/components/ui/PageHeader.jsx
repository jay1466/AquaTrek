import React from 'react';
import { Box, Typography, Breadcrumbs, Link as MuiLink } from '@mui/material';
import { Link } from 'react-router-dom';
import { NavigateNextOutlined } from '@mui/icons-material';

/**
 * Reusable page header with title, subtitle, breadcrumbs, and action slot.
 *
 * Usage:
 *   <PageHeader
 *     title="Apartments"
 *     subtitle="Manage apartment societies"
 *     breadcrumbs={[{ label: 'Home', to: '/dashboard' }, { label: 'Apartments' }]}
 *     actions={<Button>Add New</Button>}
 *   />
 */
function PageHeader({ title, subtitle, breadcrumbs = [], actions }) {
  return (
    <Box sx={{ mb: 3 }}>
      {breadcrumbs.length > 0 && (
        <Breadcrumbs
          separator={<NavigateNextOutlined fontSize="small" />}
          sx={{ mb: 1 }}
        >
          {breadcrumbs.map((crumb, i) =>
            crumb.to ? (
              <MuiLink
                key={i}
                component={Link}
                to={crumb.to}
                underline="hover"
                color="text.secondary"
                variant="body2"
              >
                {crumb.label}
              </MuiLink>
            ) : (
              <Typography key={i} variant="body2" color="text.primary" fontWeight={500}>
                {crumb.label}
              </Typography>
            )
          )}
        </Breadcrumbs>
      )}

      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={700} color="text.primary">
            {title}
          </Typography>
          {subtitle && (
            <Typography variant="body2" color="text.secondary" mt={0.5}>
              {subtitle}
            </Typography>
          )}
        </Box>
        {actions && <Box sx={{ display: 'flex', gap: 1 }}>{actions}</Box>}
      </Box>
    </Box>
  );
}

export default PageHeader;
