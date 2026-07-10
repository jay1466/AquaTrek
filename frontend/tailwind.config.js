/** @type {import('tailwindcss').Config} */
export default {
  // Files Tailwind should scan for class names
  content: [
    './index.html',
    './src/**/*.{js,jsx,ts,tsx}',
  ],

  // Enable class-based dark mode (driven by a 'dark' class on <html>)
  darkMode: 'class',

  theme: {
    extend: {
      // AquaTrack brand colour palette — matches the MUI theme
      colors: {
        primary: {
          50:  '#e3f2fd',
          100: '#bbdefb',
          200: '#90caf9',
          300: '#64b5f6',
          400: '#42a5f5',
          500: '#1976d2',   // Primary brand blue
          600: '#1565c0',
          700: '#0d47a1',
          800: '#0a3880',
          900: '#072b60',
        },
        secondary: {
          50:  '#e0f7fa',
          100: '#b2ebf2',
          200: '#80deea',
          300: '#4dd0e1',
          400: '#26c6da',
          500: '#00acc1',   // Teal accent
          600: '#00838f',
          700: '#006064',
        },
        success: {
          light: '#81c784',
          main:  '#4caf50',
          dark:  '#388e3c',
        },
        warning: {
          light: '#ffb74d',
          main:  '#ff9800',
          dark:  '#f57c00',
        },
        error: {
          light: '#e57373',
          main:  '#f44336',
          dark:  '#d32f2f',
        },
        // Dark mode backgrounds
        dark: {
          bg:      '#0f1117',
          surface: '#1a1d27',
          card:    '#21253a',
          border:  '#2d3148',
        },
      },

      // Custom font families
      fontFamily: {
        sans:  ['Inter', 'Roboto', 'system-ui', 'sans-serif'],
        mono:  ['JetBrains Mono', 'Fira Code', 'monospace'],
      },

      // Custom border radius values
      borderRadius: {
        'xl':  '0.75rem',
        '2xl': '1rem',
        '3xl': '1.5rem',
      },

      // Custom box shadows
      boxShadow: {
        'card':    '0 2px 12px 0 rgba(0, 0, 0, 0.08)',
        'card-lg': '0 4px 24px 0 rgba(0, 0, 0, 0.12)',
        'card-hover': '0 8px 32px 0 rgba(0, 0, 0, 0.16)',
      },

      // Spacing extensions
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
        '128': '32rem',
      },

      // Animation extensions
      animation: {
        'fade-in':    'fadeIn 0.3s ease-in-out',
        'slide-up':   'slideUp 0.3s ease-out',
        'pulse-slow': 'pulse 3s infinite',
      },
      keyframes: {
        fadeIn: {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%':   { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },

  plugins: [],
};
