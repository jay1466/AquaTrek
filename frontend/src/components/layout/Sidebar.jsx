import React, { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  Box, Drawer, List, ListItem, ListItemButton, ListItemIcon,
  ListItemText, Collapse, Typography, Divider, IconButton, Tooltip,
} from '@mui/material';
import {
  DashboardOutlined, ApartmentOutlined, BusinessOutlined,
  HomeWorkOutlined, WaterOutlined, ReceiptOutlined,
  PaymentOutlined, LocalShippingOutlined, NotificationsOutlined,
  BarChartOutlined, SettingsOutlined, PersonOutlined,
  ExpandLess, ExpandMore, WaterDropOutlined, ChevronLeft,
  ChevronRight, SpeedOutlined,
} from '@mui/icons-material';
import { useAuth } from '@context/AuthContext';

const SIDEBAR_WIDTH      = 260;
const SIDEBAR_COLLAPSED  = 72;

const NAV_ITEMS = [
  {
    label: 'Dashboard',
    icon:  <DashboardOutlined />,
    path:  '/dashboard',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER', 'RESIDENT'],
  },
  {
    label: 'Apartments',
    icon:  <ApartmentOutlined />,
    path:  '/apartments',
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    label: 'Buildings',
    icon:  <BusinessOutlined />,
    path:  '/buildings',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER'],
  },
  {
    label: 'Households',
    icon:  <HomeWorkOutlined />,
    path:  '/households',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER'],
  },
  {
    label: 'Water Meters',
    icon:  <WaterOutlined />,
    path:  '/meters',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER'],
  },
  { divider: true },
  {
    label: 'Billing',
    icon:  <SpeedOutlined />,
    path:  '/billing',
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    label: 'Invoices',
    icon:  <ReceiptOutlined />,
    path:  '/invoices',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER', 'RESIDENT'],
  },
  {
    label: 'Payments',
    icon:  <PaymentOutlined />,
    path:  '/payments',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER', 'RESIDENT'],
  },
  {
    label: 'Bulk Water',
    icon:  <LocalShippingOutlined />,
    path:  '/bulk-water',
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  { divider: true },
  {
    label: 'Alerts',
    icon:  <NotificationsOutlined />,
    path:  '/alerts',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER', 'RESIDENT'],
  },
  {
    label: 'Analytics',
    icon:  <BarChartOutlined />,
    path:  '/analytics',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER'],
  },
  { divider: true },
  {
    label: 'Settings',
    icon:  <SettingsOutlined />,
    path:  '/settings',
    roles: ['SUPER_ADMIN', 'ADMIN'],
  },
  {
    label: 'My Profile',
    icon:  <PersonOutlined />,
    path:  '/profile',
    roles: ['SUPER_ADMIN', 'ADMIN', 'MANAGER', 'RESIDENT'],
  },
];

function Sidebar({ open, onClose, variant = 'permanent' }) {
  const { user } = useAuth();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);

  const width = collapsed ? SIDEBAR_COLLAPSED : SIDEBAR_WIDTH;

  const canSee = (item) =>
    !item.roles || item.roles.includes(user?.role);

  const isActive = (path) =>
    location.pathname === path || location.pathname.startsWith(path + '/');

  const content = (
    <Box
      sx={{
        width,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: 'background.paper',
        borderRight: '1px solid',
        borderColor: 'divider',
        transition: 'width 0.25s ease',
        overflow: 'hidden',
      }}
    >
      {/* ── Brand ───────────────────────────────────────── */}
      <Box
        sx={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          px: collapsed ? 1.5 : 2.5,
          gap: 1.5,
          flexShrink: 0,
          borderBottom: '1px solid',
          borderColor: 'divider',
          justifyContent: collapsed ? 'center' : 'space-between',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box
            sx={{
              width: 36, height: 36,
              borderRadius: '10px',
              background: 'linear-gradient(135deg, #1976d2, #00acc1)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <WaterDropOutlined sx={{ color: '#fff', fontSize: 20 }} />
          </Box>
          {!collapsed && (
            <Box>
              <Typography variant="subtitle1" fontWeight={700} lineHeight={1.1}>
                AquaTrack
              </Typography>
              <Typography variant="caption" color="text.secondary" lineHeight={1}>
                Water Management
              </Typography>
            </Box>
          )}
        </Box>
        {!collapsed && (
          <IconButton size="small" onClick={() => setCollapsed(true)}>
            <ChevronLeft fontSize="small" />
          </IconButton>
        )}
      </Box>

      {/* Expand button when collapsed */}
      {collapsed && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 1 }}>
          <IconButton size="small" onClick={() => setCollapsed(false)}>
            <ChevronRight fontSize="small" />
          </IconButton>
        </Box>
      )}

      {/* ── Nav Items ───────────────────────────────────── */}
      <Box sx={{ flex: 1, overflowY: 'auto', overflowX: 'hidden', py: 1, px: collapsed ? 0.5 : 1.5 }}>
        {NAV_ITEMS.map((item, idx) => {
          if (item.divider) {
            return <Divider key={`div-${idx}`} sx={{ my: 1 }} />;
          }
          if (!canSee(item)) return null;

          const active = isActive(item.path);

          return collapsed ? (
            <Tooltip key={item.path} title={item.label} placement="right">
              <ListItemButton
                component={NavLink}
                to={item.path}
                sx={{
                  borderRadius: '10px', mb: 0.5, justifyContent: 'center', px: 1,
                  color: active ? 'primary.main' : 'text.secondary',
                  backgroundColor: active ? 'primary.50' : 'transparent',
                  '&:hover': { backgroundColor: 'action.hover' },
                }}
              >
                <ListItemIcon sx={{ minWidth: 0, color: 'inherit' }}>
                  {item.icon}
                </ListItemIcon>
              </ListItemButton>
            </Tooltip>
          ) : (
            <ListItemButton
              key={item.path}
              component={NavLink}
              to={item.path}
              sx={{
                borderRadius: '10px', mb: 0.5, px: 1.5, py: 1,
                color: active ? 'primary.main' : 'text.secondary',
                backgroundColor: active ? 'rgba(25,118,210,0.08)' : 'transparent',
                fontWeight: active ? 600 : 400,
                '&:hover': { backgroundColor: 'action.hover', color: 'primary.main' },
              }}
            >
              <ListItemIcon sx={{ minWidth: 36, color: 'inherit' }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{ fontSize: 14, fontWeight: active ? 600 : 500 }}
              />
            </ListItemButton>
          );
        })}
      </Box>

      {/* ── User Footer ─────────────────────────────────── */}
      {!collapsed && user && (
        <Box
          sx={{
            px: 2, py: 1.5,
            borderTop: '1px solid',
            borderColor: 'divider',
            display: 'flex',
            alignItems: 'center',
            gap: 1.5,
          }}
        >
          <Box
            sx={{
              width: 32, height: 32, borderRadius: '50%',
              background: 'linear-gradient(135deg, #1976d2, #00acc1)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <Typography variant="caption" color="white" fontWeight={700}>
              {user.fullName?.charAt(0)?.toUpperCase() || 'U'}
            </Typography>
          </Box>
          <Box sx={{ overflow: 'hidden' }}>
            <Typography variant="body2" fontWeight={600} noWrap>
              {user.fullName}
            </Typography>
            <Typography variant="caption" color="text.secondary" noWrap>
              {user.roleDisplayName || user.role}
            </Typography>
          </Box>
        </Box>
      )}
    </Box>
  );

  if (variant === 'temporary') {
    return (
      <Drawer open={open} onClose={onClose} variant="temporary"
              ModalProps={{ keepMounted: true }}
              sx={{ '& .MuiDrawer-paper': { width: SIDEBAR_WIDTH, boxSizing: 'border-box' } }}>
        {content}
      </Drawer>
    );
  }

  return (
    <Box component="nav" sx={{ width, flexShrink: 0, transition: 'width 0.25s ease' }}>
      <Drawer variant="permanent" open
              sx={{ width, '& .MuiDrawer-paper': { width, boxSizing: 'border-box', transition: 'width 0.25s ease' } }}>
        {content}
      </Drawer>
    </Box>
  );
}

export default Sidebar;
