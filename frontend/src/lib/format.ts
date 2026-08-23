import dayjs from 'dayjs';
import 'dayjs/locale/sr';

dayjs.locale('sr');

/** Backend ocekuje LocalDate: "2026-03-01". */
export const API_DATE = 'YYYY-MM-DD';
/** Backend ocekuje LocalDateTime: "2026-03-01T08:00:00" (bez vremenske zone). */
export const API_DATE_TIME = 'YYYY-MM-DDTHH:mm:ss';

export function toApiDate(value: Date | string | null | undefined): string | undefined {
  return value ? dayjs(value).format(API_DATE) : undefined;
}

export function toApiDateTime(value: Date | string | null | undefined): string | undefined {
  return value ? dayjs(value).format(API_DATE_TIME) : undefined;
}

export function formatDate(value: string | null | undefined): string {
  return value ? dayjs(value).format('DD.MM.YYYY.') : '—';
}

export function formatDateTime(value: string | null | undefined): string {
  return value ? dayjs(value).format('DD.MM.YYYY. HH:mm') : '—';
}

export function formatTime(value: string | null | undefined): string {
  return value ? dayjs(value).format('HH:mm') : '—';
}

/** Interval u istom danu se skracuje: "01.03.2026. 08:00–10:00". */
export function formatInterval(start: string, end: string): string {
  const from = dayjs(start);
  const to = dayjs(end);
  return from.isSame(to, 'day')
    ? `${from.format('DD.MM.YYYY. HH:mm')}–${to.format('HH:mm')}`
    : `${from.format('DD.MM.YYYY. HH:mm')} – ${to.format('DD.MM.YYYY. HH:mm')}`;
}

export function formatMoney(value: number | null | undefined): string {
  return value == null ? '—' : new Intl.NumberFormat('sr-RS').format(value) + ' RSD';
}

export function formatHours(value: number | null | undefined): string {
  if (value == null) return '—';
  const rounded = Math.round(value * 100) / 100;
  return `${rounded} h`;
}

/** Prazna vrednost kao crtica, da tabele ne prikazuju "null". */
export function dash(value: string | number | null | undefined): string {
  return value === null || value === undefined || value === '' ? '—' : String(value);
}
