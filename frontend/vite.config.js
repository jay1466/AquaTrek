import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

/**
 * Vite configuration for AquaTrack frontend.
 *
 * Key settings:
 * - React plugin with Fast Refresh
 * - Path aliases (@/ → src/)
 * - Dev proxy to backend API (avoids CORS issues in development)
 * - Production build optimisation with code splitting
 */
export default defineConfig(({ mode }) => {
  // Load environment variables for the current mode
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [
      react({
        // Enable React Fast Refresh for instant HMR without state loss
        fastRefresh: true,
      }),
    ],

    // Path aliases
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@components': path.resolve(__dirname, './src/components'),
        '@pages': path.resolve(__dirname, './src/pages'),
        '@api': path.resolve(__dirname, './src/api'),
        '@hooks': path.resolve(__dirname, './src/hooks'),
        '@utils': path.resolve(__dirname, './src/utils'),
        '@context': path.resolve(__dirname, './src/context'),
        '@theme': path.resolve(__dirname, './src/theme'),
        '@assets': path.resolve(__dirname, './src/assets'),
      },
    },

    // Development server
    server: {
      port: 5173,
      host: true,        // Listen on all interfaces (needed for Docker)
      open: false,
      proxy: {
        // Proxy all /api requests to the Spring Boot backend
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
        // Proxy actuator endpoints
        '/actuator': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
      },
    },

    // Production build
    build: {
      outDir: 'dist',
      sourcemap: mode !== 'production',
      // Split vendor chunks for better caching
      rollupOptions: {
        output: {
          manualChunks: {
            // MUI is large — split it separately
            'vendor-mui': ['@mui/material', '@mui/icons-material', '@emotion/react', '@emotion/styled'],
            // Recharts for charts
            'vendor-recharts': ['recharts'],
            // React core
            'vendor-react': ['react', 'react-dom', 'react-router-dom'],
            // Data layer
            'vendor-query': ['@tanstack/react-query', 'axios'],
          },
        },
      },
      // Warn if any chunk exceeds 700 KB
      chunkSizeWarningLimit: 700,
    },

    // Make env variables available in the app
    define: {
      __APP_VERSION__: JSON.stringify(process.env.npm_package_version),
    },
  };
});
