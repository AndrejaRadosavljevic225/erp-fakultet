import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { MantineProvider, createTheme } from '@mantine/core';
import { DatesProvider } from '@mantine/dates';
import { ModalsProvider } from '@mantine/modals';
import 'dayjs/locale/sr';
import { Notifications } from '@mantine/notifications';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

import '@mantine/core/styles.css';
import '@mantine/dates/styles.css';
import '@mantine/notifications/styles.css';
import '@mantine/charts/styles.css';
import './index.css';

import App from './App';
import { AuthProvider } from './auth/AuthContext';

const theme = createTheme({
  primaryColor: 'indigo',
  defaultRadius: 'md',
});

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 30_000,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MantineProvider theme={theme} defaultColorScheme="auto" env={import.meta.env.VITE_NO_ANIM ? 'test' : undefined}>
      <QueryClientProvider client={queryClient}>
        {/* Kalendari na srpskom, nedelja pocinje ponedeljkom */}
        <DatesProvider settings={{ locale: 'sr', firstDayOfWeek: 1, consistentWeeks: true }}>
          <ModalsProvider>
            <Notifications position="top-right" />
            <BrowserRouter>
              <AuthProvider>
                <App />
              </AuthProvider>
            </BrowserRouter>
          </ModalsProvider>
        </DatesProvider>
      </QueryClientProvider>
    </MantineProvider>
  </StrictMode>,
);
