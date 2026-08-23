import { notifications } from '@mantine/notifications';
import { modals } from '@mantine/modals';
import { errorMessage } from '../api/client';

export function notifySuccess(message: string) {
  notifications.show({ color: 'green', title: 'Uspešno', message });
}

export function notifyError(error: unknown, fallback?: string) {
  notifications.show({ color: 'red', title: 'Greška', message: errorMessage(error, fallback) });
}

/** Potvrda pre nepovratne akcije (brisanje, otkazivanje rezervacije...). */
export function confirmAction({
  title,
  message,
  confirmLabel = 'Potvrdi',
  danger = true,
  onConfirm,
}: {
  title: string;
  message: string;
  confirmLabel?: string;
  danger?: boolean;
  onConfirm: () => void;
}) {
  modals.openConfirmModal({
    title,
    children: message,
    labels: { confirm: confirmLabel, cancel: 'Odustani' },
    confirmProps: danger ? { color: 'red' } : undefined,
    onConfirm,
  });
}
