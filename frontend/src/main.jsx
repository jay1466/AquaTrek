import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { SnackbarProvider } from 'notistack';

import App from './App';
import { ThemeModeProvider } from '@context/ThemeContext';
import '@/styles/index.css';

/**
 * React Query client configuration.
 *
 * - staleTime: 5 minutes — data stays fresh for 5 minutes before refetch
 * - gcTime: 30 minutes — cached data is kept for 30 minutes
 * - retry: 1 — only retry failed requests once
 * - refetchOnWindowFocus: false — disable automatic refetch on window focus
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime:            5 * 60 * 1000,   // 5 minutes
      gcTime:              30 * 60 * 1000,   // 30 minutes (formerly cacheTime)
      retry:               1,
      refetchOnWindowFocus: false,
      refetchOnMount:      true,
    },
    mutations: {
      retry: 0,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    {/* BrowserRouter — enables React Router v6 */}
    <BrowserRouter>
      {/* React Query — global data fetching and caching */}
      <QueryClientProvider client={queryClient}>
        {/* Theme — MUI + Tailwind dark/light mode */}
        <ThemeModeProvider>
          {/* Notistack — global toast notification system */}
          <SnackbarProvider
            maxSnack={4}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            autoHideDuration={4000}
            preventDuplicate
          >
            <App />
          </SnackbarProvider>
        </ThemeModeProvider>

        {/* React Query Devtools — only in development */}
        {import.meta.env.DEV && (
          <ReactQueryDevtools initialIsOpen={false} position="bottom" />
        )}
      </QueryClientProvider>
    </BrowserRouter>
  </React.StrictMode>
);
