import React, { createContext, useContext, useMemo, useState, useEffect, useCallback } from 'react';
import { ThemeProvider as MuiThemeProvider, CssBaseline } from '@mui/material';
import createAquaTheme from '@theme/theme';

/**
 * AquaTrack Theme Context.
 *
 * Provides:
 * - `mode`         — current theme mode ('light' | 'dark')
 * - `toggleTheme`  — function to switch between light and dark
 * - `isDark`       — boolean convenience flag
 *
 * Persistence:
 * - The user's preference is stored in localStorage under 'aquatrack-theme'.
 * - On first load, uses the OS-level preference (prefers-color-scheme).
 *
 * Usage:
 *   const { isDark, toggleTheme } = useThemeMode();
 */

const ThemeModeContext = createContext({
  mode:        'light',
  toggleTheme: () => {},
  isDark:      false,
});

const STORAGE_KEY = 'aquatrack-theme';

/**
 * Detects the initial theme preference:
 * 1. Check localStorage for a previously saved preference.
 * 2. Fall back to the OS-level prefers-color-scheme media query.
 * 3. Default to 'light'.
 */
function getInitialMode() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === 'light' || saved === 'dark') return saved;
  } catch {
    // localStorage may be unavailable (e.g., private browsing)
  }

  if (window.matchMedia?.('(prefers-color-scheme: dark)').matches) {
    return 'dark';
  }

  return 'light';
}

/**
 * ThemeModeProvider wraps the application with both the MUI ThemeProvider
 * and the custom theme mode context so components can toggle dark mode.
 */
export function ThemeModeProvider({ children }) {
  const [mode, setMode] = useState(getInitialMode);

  // Sync the 'dark' class on <html> for Tailwind's class-based dark mode
  useEffect(() => {
    const root = document.documentElement;
    if (mode === 'dark') {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
    // Persist preference
    try {
      localStorage.setItem(STORAGE_KEY, mode);
    } catch {
      // Ignore storage errors
    }
  }, [mode]);

  const toggleTheme = useCallback(() => {
    setMode((prev) => (prev === 'light' ? 'dark' : 'light'));
  }, []);

  // Memoize the context value to prevent unnecessary re-renders
  const contextValue = useMemo(
    () => ({ mode, toggleTheme, isDark: mode === 'dark' }),
    [mode, toggleTheme]
  );

  // Build the MUI theme whenever the mode changes
  const muiTheme = useMemo(() => createAquaTheme(mode), [mode]);

  return (
    <ThemeModeContext.Provider value={contextValue}>
      <MuiThemeProvider theme={muiTheme}>
        {/* CssBaseline applies MUI's baseline CSS reset */}
        <CssBaseline />
        {children}
      </MuiThemeProvider>
    </ThemeModeContext.Provider>
  );
}

/**
 * Hook for consuming the theme mode context.
 *
 * Must be used inside a <ThemeModeProvider>.
 *
 * @returns {{ mode: string, toggleTheme: Function, isDark: boolean }}
 */
export function useThemeMode() {
  const context = useContext(ThemeModeContext);
  if (!context) {
    throw new Error('useThemeMode must be used within a ThemeModeProvider');
  }
  return context;
}

export default ThemeModeContext;
