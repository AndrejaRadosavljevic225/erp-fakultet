import { Suspense, lazy } from 'react';
import { Center, Loader } from '@mantine/core';
import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from './components/AppLayout';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { LoginPage } from './features/auth/LoginPage';
import { DashboardPage } from './features/dashboard/DashboardPage';

// Ostali ekrani se ucitavaju tek kad zatrebaju — kalendar (FullCalendar) i administracija
// inace nepotrebno uvecavaju prvi bundle.
const ChangePasswordPage = lazy(() =>
  import('./features/auth/ChangePasswordPage').then((m) => ({ default: m.ChangePasswordPage })),
);
const WorkersPage = lazy(() => import('./features/hr/WorkersPage').then((m) => ({ default: m.WorkersPage })));
const WorkerDetailPage = lazy(() =>
  import('./features/hr/WorkerDetailPage').then((m) => ({ default: m.WorkerDetailPage })),
);
const PositionsPage = lazy(() => import('./features/hr/PositionsPage').then((m) => ({ default: m.PositionsPage })));
const RoomsPage = lazy(() => import('./features/schedule/RoomsPage').then((m) => ({ default: m.RoomsPage })));
const BookingsPage = lazy(() => import('./features/schedule/BookingsPage').then((m) => ({ default: m.BookingsPage })));
const CalendarPage = lazy(() => import('./features/schedule/CalendarPage').then((m) => ({ default: m.CalendarPage })));
const ApprovalsPage = lazy(() =>
  import('./features/schedule/ApprovalsPage').then((m) => ({ default: m.ApprovalsPage })),
);
const SchoolYearsPage = lazy(() =>
  import('./features/teaching/SchoolYearsPage').then((m) => ({ default: m.SchoolYearsPage })),
);
const TeachingPage = lazy(() => import('./features/teaching/TeachingPage').then((m) => ({ default: m.TeachingPage })));
const UsersPage = lazy(() => import('./features/admin/UsersPage').then((m) => ({ default: m.UsersPage })));
const RolesPage = lazy(() => import('./features/admin/RolesPage').then((m) => ({ default: m.RolesPage })));
const PermissionsPage = lazy(() =>
  import('./features/admin/PermissionsPage').then((m) => ({ default: m.PermissionsPage })),
);
const AuditLogPage = lazy(() => import('./features/admin/AuditLogPage').then((m) => ({ default: m.AuditLogPage })));

function PageLoader() {
  return (
    <Center py="xl">
      <Loader />
    </Center>
  );
}

export default function App() {
  return (
    <Suspense fallback={<PageLoader />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route index element={<DashboardPage />} />
            <Route path="change-password" element={<ChangePasswordPage />} />

            {/* Kadrovi */}
            <Route path="workers" element={<WorkersPage />} />
            <Route path="workers/:id" element={<WorkerDetailPage />} />

            {/* Raspored */}
            <Route path="rooms" element={<RoomsPage />} />
            <Route path="bookings" element={<BookingsPage />} />
            <Route path="calendar" element={<CalendarPage />} />

            {/* Nastava */}
            <Route path="teaching" element={<TeachingPage />} />

            {/* Ekrani samo za ADMIN i HR */}
            <Route element={<ProtectedRoute roles={['ADMIN', 'HR']} />}>
              <Route path="positions" element={<PositionsPage />} />
              <Route path="approvals" element={<ApprovalsPage />} />
              <Route path="school-years" element={<SchoolYearsPage />} />
              <Route path="users" element={<UsersPage />} />
              <Route path="roles" element={<RolesPage />} />
              <Route path="permissions" element={<PermissionsPage />} />
              <Route path="audit-logs" element={<AuditLogPage />} />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}
