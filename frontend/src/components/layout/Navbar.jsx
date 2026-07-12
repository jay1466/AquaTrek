import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  AppBar, Toolbar, IconButton, Typography, Box, Avatar,
  Menu, MenuItem, Divider, ListItemIcon, Tooltip, Badge,
  useMediaQuery, useTheme,
} from '@mui/material';
import {
  MenuOutlined, NotificationsOutlined, Brightness4Outlined,
  Brightness7Outlined, PersonOutlined, LogoutOutlined,
  SettingsOutlined,
} from '@mui/icons-material';
import { useAuth }       from '@context/AuthContext';
import { useThemeMode }  from '@context/ThemeContext';

function Navbar({ onMenuClick }) {
  const { user, logout }       = useAuth();
  const { isDark, toggleTheme } = useThemeMode();
  const navigate               = useNavigate();
  const muiTheme               = useTheme();
  const isMobile               = useMediaQuery(muiTheme.breakpoints.down('md'));

  const [anchorEl, setAnchorEl] = useState(null);
  const menuOpen = Boolean(anchorEl);

  const handleAvatarClick = (e) => setAnchorEl(e.currentTarget);
  const handleMenuClose   = () => setAnchorEl(null);

  const handleLogout = async () => {
    handleMenuClose();
    await logout();
  };

  const initials = user?.fullName
    ? user.fullName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
    : 'U';

  return (
    <AppBar
      position="sticky"
      elevation={0}
      sx={{
        backgroundColor: 'background.paper',
        color: 'text.primary',
        borderBottom: '1px solid',
        borderColor: 'divider',
        zIndex: (t) => t.zIndex.drawer - 1,
      }}
    >
      <Toolbar sx={{ gap: 1, minHeight: 64 }}>

        {/* Mobile hamburger */}
        {isMobile && (
          <IconButton edge="start" onClick={onMenuClick} size="medium">
            <MenuOutlined />
          </IconButton>
        )}

        {/* Page title — filled by children via document.title in each page */}
        <Typography variant="h6" fontWeight={600} sx={{ flexGrow: 1 }}>
          {document.title?.replace(' — AquaTrack', '') || 'AquaTrack'}
        </Typography>

        {/* ── Right controls ─────────────────────────── */}

        {/* Dark mode toggle */}
        <Tooltip title={isDark ? 'Switch to light mode' : 'Switch to dark mode'}>
          <IconButton onClick={toggleTheme} size="medium">
            {isDark ? <Brightness7Outlined /> : <Brightness4Outlined />}
          </IconButton>
        </Tooltip>

        {/* Notifications */}
        <Tooltip title="Notifications">
          <IconButton size="medium" onClick={() => navigate('/alerts')}>
            <Badge badgeContent={0} color="error">
              <NotificationsOutlined />
            </Badge>
          </IconButton>
        </Tooltip>

        {/* User avatar */}
        <Tooltip title="Account">
          <IconButton onClick={handleAvatarClick} size="small" sx={{ ml: 0.5 }}>
            <Avatar
              src={user?.profilePhotoUrl}
              sx={{
                width: 36, height: 36, fontSize: 14, fontWeight: 700,
                background: 'linear-gradient(135deg, #1976d2, #00acc1)',
              }}
            >
              {initials}
            </Avatar>
          </IconButton>
        </Tooltip>

        {/* User dropdown menu */}
        <Menu
          anchorEl={anchorEl}
          open={menuOpen}
          onClose={handleMenuClose}
          transformOrigin={{ horizontal: 'right', vertical: 'top' }}
          anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          PaperProps={{
            elevation: 3,
            sx: { mt: 1, minWidth: 200, borderRadius: '12px' },
          }}
        >
          {/* User info */}
          <Box sx={{ px: 2, py: 1.5 }}>
            <Typography variant="subtitle2" fontWeight={700}>{user?.fullName}</Typography>
            <Typography variant="caption" color="text.secondary">{user?.email}</Typography>
          </Box>

          <Divider />

          <MenuItem onClick={() => { handleMenuClose(); navigate('/profile'); }}>
            <ListItemIcon><PersonOutlined fontSize="small" /></ListItemIcon>
            My Profile
          </MenuItem>

          <MenuItem onClick={() => { handleMenuClose(); navigate('/settings'); }}>
            <ListItemIcon><SettingsOutlined fontSize="small" /></ListItemIcon>
            Settings
          </MenuItem>

          <Divider />

          <MenuItem onClick={handleLogout} sx={{ color: 'error.main' }}>
            <ListItemIcon><LogoutOutlined fontSize="small" color="error" /></ListItemIcon>
            Sign Out
          </MenuItem>
        </Menu>

      </Toolbar>
    </AppBar>
  );
}

export default Navbar;
