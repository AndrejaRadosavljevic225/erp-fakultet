import type { EmploymentStatus, EmploymentType } from '../types/hr';
import type { BookingStatus, TeachingType } from '../types/schedule';

export const employmentStatusLabels: Record<EmploymentStatus, string> = {
  ACTIVE: 'Aktivan',
  ON_LEAVE: 'Na odsustvu',
  TERMINATED: 'Prestao radni odnos',
  SUSPENDED: 'Suspendovan',
};

export const employmentStatusColors: Record<EmploymentStatus, string> = {
  ACTIVE: 'green',
  ON_LEAVE: 'yellow',
  TERMINATED: 'gray',
  SUSPENDED: 'red',
};

export const employmentTypeLabels: Record<EmploymentType, string> = {
  FULL_TIME: 'Puno radno vreme',
  PART_TIME: 'Nepuno radno vreme',
  CONTRACT: 'Ugovor',
  TEMPORARY: 'Privremeno',
};

export const bookingStatusLabels: Record<BookingStatus, string> = {
  REQUESTED: 'Čeka odobrenje',
  ACCEPTED: 'Odobrena',
  REJECTED: 'Odbijena',
  CANCELLED: 'Otkazana',
  FINISHED: 'Održana',
};

export const bookingStatusColors: Record<BookingStatus, string> = {
  REQUESTED: 'yellow',
  ACCEPTED: 'green',
  REJECTED: 'red',
  CANCELLED: 'gray',
  FINISHED: 'blue',
};

export const teachingTypeLabels: Record<TeachingType, string> = {
  REGULAR: 'Redovni čas',
  EXTRA: 'Dodatni čas',
  MENTORSHIP: 'Mentorstvo',
  OTHER: 'Ostalo (nastavno)',
};

export const roleLabels: Record<string, string> = {
  ADMIN: 'Administrator',
  HR: 'HR služba',
  PROFESOR: 'Profesor',
};

/** Pomocna funkcija za Mantine Select: enum mapa -> [{value, label}]. */
export function toSelectData<T extends string>(labels: Record<T, string>) {
  return (Object.entries(labels) as [T, string][]).map(([value, label]) => ({ value, label }));
}
