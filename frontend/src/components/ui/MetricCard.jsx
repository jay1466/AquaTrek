import React from 'react';
import { Box, Card, CardContent, Typography, Skeleton } from '@mui/material';
import { TrendingUpOutlined, TrendingDownOutlined } from '@mui/icons-material';

/**
 * Dashboard metric card with icon, value, label, and optional trend indicator.
 *
 * Usage:
 *   <MetricCard
 *     title="Total Units"
 *     value={120}
 *     icon={<HomeWorkOutlined />}
 *     color="#1976d2"
 *     trend={{ value: 5, positive: true, label: 'vs last month' }}
 *     loading={false}
 *   />
 */
function MetricCard({ title, value, icon, color = '#1976d2', trend, loading = false, suffix = '' }) {
  return (
    <Card
      elevation={0}
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: '16px',
        height: '100%',
        transition: 'box-shadow 0.2s ease, transform 0.2s ease',
        '&:hover': {
          boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
          transform: 'translateY(-2px)',
        },
      }}
    >
      <CardContent sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          {/* Icon */}
          <Box
            sx={{
              width: 48, height: 48,
              borderRadius: '12px',
              backgroundColor: `${color}18`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color,
            }}
          >
            {icon}
          </Box>

          {/* Trend */}
          {trend && !loading && (
            <Box
              sx={{
                display: 'flex', alignItems: 'center', gap: 0.3,
                color: trend.positive ? 'success.main' : 'error.main',
                backgroundColor: trend.positive ? 'rgba(76,175,80,0.1)' : 'rgba(244,67,54,0.1)',
                px: 1, py: 0.3, borderRadius: '20px',
              }}
            >
              {trend.positive
                ? <TrendingUpOutlined sx={{ fontSize: 14 }} />
                : <TrendingDownOutlined sx={{ fontSize: 14 }} />
              }
              <Typography variant="caption" fontWeight={600}>
                {trend.value}%
              </Typography>
            </Box>
          )}
        </Box>

        {/* Value */}
        {loading ? (
          <>
            <Skeleton variant="text" width="60%" height={40} />
            <Skeleton variant="text" width="80%" />
          </>
        ) : (
          <>
            <Typography variant="h4" fontWeight={800} color="text.primary" lineHeight={1.2}>
              {typeof value === 'number' ? value.toLocaleString() : value}
              {suffix && <Typography component="span" variant="body1" color="text.secondary" ml={0.5}>{suffix}</Typography>}
            </Typography>
            <Typography variant="body2" color="text.secondary" mt={0.5} fontWeight={500}>
              {title}
            </Typography>
            {trend?.label && (
              <Typography variant="caption" color="text.disabled">
                {trend.label}
              </Typography>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}

export default MetricCard;
