import { createTheme, alpha } from '@mui/material/styles';

/**
 * Creates the AquaTrack MUI theme for the given colour mode.
 *
 * The theme is designed to work alongside Tailwind CSS:
 * - Colours match the Tailwind custom palette in tailwind.config.js
 * - Typography uses Inter (loaded in index.html)
 * - Component overrides ensure MUI controls look native to the design system
 *
 * @param {'light' | 'dark'} mode
 * @returns MUI Theme object
 */
const createAquaTheme = (mode) => {
  const isLight = mode === 'light';

  return createTheme({
    palette: {
      mode,
      primary: {
        light:   '#42a5f5',
        main:    '#1976d2',
        dark:    '#0d47a1',
        contrastText: '#ffffff',
      },
      secondary: {
        light:   '#4dd0e1',
        main:    '#00acc1',
        dark:    '#006064',
        contrastText: '#ffffff',
      },
      success: {
        main:  '#4caf50',
        light: '#81c784',
        dark:  '#388e3c',
      },
      warning: {
        main:  '#ff9800',
        light: '#ffb74d',
        dark:  '#f57c00',
      },
      error: {
        main:  '#f44336',
        light: '#e57373',
        dark:  '#d32f2f',
      },
      info: {
        main: '#2196f3',
      },
      background: {
        default: isLight ? '#f4f6f9' : '#0f1117',
        paper:   isLight ? '#ffffff' : '#1a1d27',
      },
      divider: isLight ? '#e0e0e0' : '#2d3148',
      text: {
        primary:   isLight ? '#1a1a2e' : '#f1f5f9',
        secondary: isLight ? '#64748b' : '#94a3b8',
        disabled:  isLight ? '#94a3b8' : '#475569',
      },
    },

    typography: {
      fontFamily: "'Inter', 'Roboto', system-ui, sans-serif",
      h1: { fontSize: '2.25rem', fontWeight: 700, lineHeight: 1.2 },
      h2: { fontSize: '1.875rem', fontWeight: 700, lineHeight: 1.3 },
      h3: { fontSize: '1.5rem',   fontWeight: 600, lineHeight: 1.3 },
      h4: { fontSize: '1.25rem',  fontWeight: 600, lineHeight: 1.4 },
      h5: { fontSize: '1.125rem', fontWeight: 600, lineHeight: 1.4 },
      h6: { fontSize: '1rem',     fontWeight: 600, lineHeight: 1.5 },
      subtitle1: { fontSize: '0.9375rem', fontWeight: 500 },
      subtitle2: { fontSize: '0.875rem',  fontWeight: 500 },
      body1: { fontSize: '0.9375rem', lineHeight: 1.6 },
      body2: { fontSize: '0.875rem',  lineHeight: 1.6 },
      caption: { fontSize: '0.75rem', color: isLight ? '#64748b' : '#94a3b8' },
      button: { fontWeight: 600, textTransform: 'none', letterSpacing: '0.01em' },
    },

    shape: {
      borderRadius: 12,  // Matches rounded-xl in Tailwind
    },

    shadows: [
      'none',
      '0 2px 4px rgba(0,0,0,0.06)',
      '0 2px 12px rgba(0,0,0,0.08)',
      '0 4px 16px rgba(0,0,0,0.1)',
      '0 4px 24px rgba(0,0,0,0.12)',
      '0 8px 32px rgba(0,0,0,0.14)',
      ...Array(19).fill('none'),  // MUI requires 25 shadow levels
    ],

    components: {
      // ── Button ──────────────────────────────────────────────
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: '10px',
            fontWeight: 600,
            textTransform: 'none',
            padding: '8px 20px',
            boxShadow: 'none',
            '&:hover': { boxShadow: 'none' },
          },
          contained: {
            '&:hover': {
              transform: 'translateY(-1px)',
              transition: 'transform 150ms ease',
            },
          },
        },
        defaultProps: {
          disableElevation: true,
        },
      },

      // ── Card ────────────────────────────────────────────────
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: '16px',
            boxShadow: isLight
              ? '0 2px 12px rgba(0,0,0,0.08)'
              : '0 2px 12px rgba(0,0,0,0.4)',
            border: `1px solid ${isLight ? '#f0f0f0' : '#2d3148'}`,
          },
        },
      },

      // ── TextField ───────────────────────────────────────────
      MuiTextField: {
        defaultProps: { variant: 'outlined', size: 'medium' },
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root': {
              borderRadius: '10px',
            },
          },
        },
      },

      // ── Paper ───────────────────────────────────────────────
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',  // Disable MUI's dark mode gradient
          },
          rounded: {
            borderRadius: '16px',
          },
        },
      },

      // ── Table ───────────────────────────────────────────────
      MuiTableHead: {
        styleOverrides: {
          root: {
            '& .MuiTableCell-head': {
              fontWeight: 600,
              fontSize: '0.8125rem',
              textTransform: 'uppercase',
              letterSpacing: '0.05em',
              backgroundColor: isLight ? '#f8fafc' : '#21253a',
              color: isLight ? '#64748b' : '#94a3b8',
            },
          },
        },
      },

      // ── Chip ────────────────────────────────────────────────
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: '8px',
            fontWeight: 500,
            fontSize: '0.75rem',
          },
        },
      },

      // ── AppBar ──────────────────────────────────────────────
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            boxShadow: 'none',
            borderBottom: `1px solid ${isLight ? '#e0e0e0' : '#2d3148'}`,
          },
        },
        defaultProps: {
          elevation: 0,
        },
      },

      // ── Dialog ──────────────────────────────────────────────
      MuiDialog: {
        styleOverrides: {
          paper: {
            borderRadius: '20px',
          },
        },
      },

      // ── Tooltip ─────────────────────────────────────────────
      MuiTooltip: {
        styleOverrides: {
          tooltip: {
            borderRadius: '8px',
            fontSize: '0.75rem',
          },
        },
        defaultProps: {
          arrow: true,
        },
      },

      // ── Drawer (sidebar) ────────────────────────────────────
      MuiDrawer: {
        styleOverrides: {
          paper: {
            border: 'none',
            backgroundImage: 'none',
          },
        },
      },

      // ── LinearProgress ──────────────────────────────────────
      MuiLinearProgress: {
        styleOverrides: {
          root: {
            borderRadius: '4px',
            height: '6px',
          },
        },
      },
    },
  });
};

export default createAquaTheme;
