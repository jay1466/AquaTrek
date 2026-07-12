import React, { useEffect } from 'react';
import { Box, Grid, Typography, Card, CardContent, Divider, List,
         ListItem, ListItemText, ListItemIcon, Chip } from '@mui/material';
import {
  ApartmentOutlined, HomeWorkOutlined, WaterOutlined,
  ReceiptOutlined, TrendingUpOutlined, CheckCircleOutlined,
  WarningAmberOutlined, ErrorOutlined,
} from '@mui/icons-material';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, BarChart, Bar, Legend,
} from 'recharts';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@context/AuthContext';
import MetricCard  from '@components/ui/MetricCard';
import apartmentApi from '@api/apartmentApi';

// ── Mock data (replaced by real API calls in later modules) ───
const TREND_DATA = [
  { month: 'Jan', usage: 2400, cost: 18000 },
  { month: 'Feb', usage: 1398, cost: 12000 },
  { month: 'Mar', usage: 3800, cost: 28000 },
  { month: 'Apr', usage: 3908, cost: 29000 },
  { month: 'May', usage: 4800, cost: 36000 },
  { month: 'Jun', usage: 3800, cost: 28500 },
  { month: 'Jul', usage: 4300, cost: 32000 },
];

const TOP_CONSUMERS = [
  { unit: 'A-201', name: 'Mehta Family',  usage: 28.5 },
  { unit: 'B-102', name: 'Singh Family',  usage: 24.2 },
  { unit: 'A-305', name: 'Kumar Family',  usage: 22.8 },
  { unit: 'C-401', name: 'Patel Family',  usage: 21.1 },
  { unit: 'B-203', name: 'Shah Family',   usage: 19.7 },
];

const RECENT_ALERTS = [
  { type: 'warning', message: 'High usage in Unit A-201 — 28.5 KL this month', time: '2h ago' },
  { type: 'error',   message: 'Payment overdue for Unit B-305 — ₹2,400',        time: '5h ago' },
  { type: 'success', message: 'Invoice generated for 98 units',                  time: '1d ago' },
  { type: 'warning', message: 'Meter MTR-00042 — reading missing',               time: '2d ago' },
];

const ALERT_ICON = {
  warning: <WarningAmberOutlined color="warning" fontSize="small" />,
  error:   <ErrorOutlined color="error" fontSize="small"   />,
  success: <CheckCircleOutlined color="success" fontSize="small" />,
};

function DashboardPage() {
  const { user, isAdmin } = useAuth();

  useEffect(() => {
    document.title = 'Dashboard — AquaTrack';
  }, []);

  // Fetch the current apartment details
  const { data: aptData, isLoading: aptLoading } = useQuery({
    queryKey: ['myApartment'],
    queryFn:  () => apartmentApi.getMyApartment(),
    enabled:  !!user && user.role !== 'SUPER_ADMIN',
    select:   (r) => r.data,
  });

  const apt = aptData;

  return (
    <Box className="animate-fade-in">

      {/* ── Welcome Banner ────────────────────────────── */}
      <Box
        sx={{
          borderRadius: '16px', mb: 3, p: 3,
          background: 'linear-gradient(135deg, #1976d2 0%, #00acc1 100%)',
          color: 'white',
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          flexWrap: 'wrap', gap: 2,
        }}
      >
        <Box>
          <Typography variant="h5" fontWeight={700}>
            Good morning, {user?.fullName?.split(' ')[0]} 👋
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.85, mt: 0.5 }}>
            {apt?.name || 'Your Apartment Society'} — Water Management Dashboard
          </Typography>
        </Box>
        <Chip
          label={`Plan: ${apt?.subscriptionPlan || 'BASIC'}`}
          sx={{ backgroundColor: 'rgba(255,255,255,0.2)', color: 'white', fontWeight: 600 }}
        />
      </Box>

      {/* ── Metric Cards ──────────────────────────────── */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        {[
          {
            title: 'Total Units',
            value: apt?.totalUnits ?? '—',
            icon:  <HomeWorkOutlined />,
            color: '#1976d2',
          },
          {
            title: 'Total Buildings',
            value: apt?.totalBuildings ?? '—',
            icon:  <ApartmentOutlined />,
            color: '#00acc1',
          },
          {
            title: 'This Month Usage',
            value: 2840,
            suffix: 'KL',
            icon:  <WaterOutlined />,
            color: '#388e3c',
            trend: { value: 12, positive: false, label: 'vs last month' },
          },
          {
            title: 'Pending Invoices',
            value: 14,
            icon:  <ReceiptOutlined />,
            color: '#f57c00',
            trend: { value: 8, positive: false, label: 'overdue' },
          },
        ].map((card) => (
          <Grid item xs={12} sm={6} md={3} key={card.title}>
            <MetricCard {...card} loading={aptLoading} />
          </Grid>
        ))}
      </Grid>

      {/* ── Charts Row ────────────────────────────────── */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>

        {/* Monthly Usage Trend */}
        <Grid item xs={12} md={8}>
          <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '16px', p: 0.5 }}>
            <CardContent>
              <Typography variant="h6" fontWeight={700} mb={0.5}>
                Monthly Water Usage
              </Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>
                Consumption trend over the last 7 months (KL)
              </Typography>
              <ResponsiveContainer width="100%" height={240}>
                <AreaChart data={TREND_DATA} margin={{ top: 5, right: 10, left: 0, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorUsage" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%"  stopColor="#1976d2" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#1976d2" stopOpacity={0.02} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip
                    contentStyle={{ borderRadius: 10, border: '1px solid #e0e0e0' }}
                    formatter={(v) => [`${v} KL`, 'Usage']}
                  />
                  <Area
                    type="monotone"
                    dataKey="usage"
                    stroke="#1976d2"
                    strokeWidth={2.5}
                    fill="url(#colorUsage)"
                    dot={{ r: 4, fill: '#1976d2' }}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Top Consumers */}
        <Grid item xs={12} md={4}>
          <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '16px', height: '100%' }}>
            <CardContent>
              <Typography variant="h6" fontWeight={700} mb={0.5}>
                Top Consumers
              </Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>
                Highest usage this month
              </Typography>
              <List dense disablePadding>
                {TOP_CONSUMERS.map((c, i) => (
                  <React.Fragment key={c.unit}>
                    <ListItem disableGutters sx={{ py: 0.8 }}>
                      <Box
                        sx={{
                          width: 24, height: 24, borderRadius: '50%',
                          backgroundColor: i === 0 ? '#f57c00' : i === 1 ? '#78909c' : '#90a4ae',
                          display: 'flex', alignItems: 'center', justifyContent: 'center',
                          mr: 1.5, flexShrink: 0,
                        }}
                      >
                        <Typography variant="caption" color="white" fontWeight={700}>
                          {i + 1}
                        </Typography>
                      </Box>
                      <ListItemText
                        primary={c.name}
                        secondary={`Unit ${c.unit}`}
                        primaryTypographyProps={{ fontSize: 13, fontWeight: 600 }}
                        secondaryTypographyProps={{ fontSize: 11 }}
                      />
                      <Typography variant="body2" fontWeight={700} color="primary.main">
                        {c.usage} KL
                      </Typography>
                    </ListItem>
                    {i < TOP_CONSUMERS.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>

      </Grid>

      {/* ── Bottom Row ────────────────────────────────── */}
      <Grid container spacing={2.5}>

        {/* Monthly Cost Bar Chart */}
        <Grid item xs={12} md={7}>
          <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '16px' }}>
            <CardContent>
              <Typography variant="h6" fontWeight={700} mb={0.5}>
                Billing Overview
              </Typography>
              <Typography variant="body2" color="text.secondary" mb={2}>
                Monthly water billing amount (₹)
              </Typography>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={TREND_DATA} margin={{ top: 5, right: 10, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                  <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `₹${(v/1000).toFixed(0)}k`} />
                  <Tooltip
                    contentStyle={{ borderRadius: 10 }}
                    formatter={(v) => [`₹${v.toLocaleString()}`, 'Billed']}
                  />
                  <Bar dataKey="cost" fill="#00acc1" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Alerts */}
        <Grid item xs={12} md={5}>
          <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: '16px', height: '100%' }}>
            <CardContent>
              <Typography variant="h6" fontWeight={700} mb={2}>
                Recent Alerts
              </Typography>
              <List dense disablePadding>
                {RECENT_ALERTS.map((a, i) => (
                  <React.Fragment key={i}>
                    <ListItem disableGutters sx={{ py: 1, alignItems: 'flex-start' }}>
                      <ListItemIcon sx={{ minWidth: 32, mt: 0.3 }}>
                        {ALERT_ICON[a.type]}
                      </ListItemIcon>
                      <ListItemText
                        primary={a.message}
                        secondary={a.time}
                        primaryTypographyProps={{ fontSize: 13, lineHeight: 1.4 }}
                        secondaryTypographyProps={{ fontSize: 11 }}
                      />
                    </ListItem>
                    {i < RECENT_ALERTS.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>

      </Grid>
    </Box>
  );
}

export default DashboardPage;
