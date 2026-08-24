import { useMemo, useState } from 'react';
import { Badge, Card, Group, NumberInput, Select, Text } from '@mantine/core';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import type { DatesSetArg, EventInput } from '@fullcalendar/core';
import srLocale from '@fullcalendar/core/locales/sr';
import { useQuery } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import { bookingsApi, roomsApi } from '../../api/schedule';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { bookingStatusLabels } from '../../lib/labels';
import { API_DATE_TIME } from '../../lib/format';
import dayjs from 'dayjs';
import type { Booking, BookingStatus } from '../../types/schedule';

/**
 * FullCalendar-ov "sr" ima latinicne nazive dugmadi, ali nazive dana i meseci uzima iz
 * Intl-a po kodu "sr" — a to je cirilica. Zato kod menjamo u "sr-Latn" i doteramo tekstove.
 */
const srLatnLocale = {
  ...srLocale,
  code: 'sr-Latn',
  buttonText: { ...srLocale.buttonText, today: 'Danas', month: 'Mesec', week: 'Nedelja', day: 'Dan' },
  noEventsText: 'Nema termina za prikaz',
  allDayText: 'Ceo dan',
};

/** Boje termina u kalendaru po statusu rezervacije. */
const STATUS_COLORS: Record<BookingStatus, string> = {
  REQUESTED: '#f59f00',
  ACCEPTED: '#2f9e44',
  FINISHED: '#1971c2',
  REJECTED: '#e03131',
  CANCELLED: '#868e96',
};

/** Kalendar zauzetosti prostorija sa filterima po sali, zgradi i kapacitetu (UC-SC-05). */
export function CalendarPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const roomId = searchParams.get('roomId');
  const [building, setBuilding] = useState<string | null>(null);
  const [minCapacity, setMinCapacity] = useState<number | string>('');
  const [range, setRange] = useState<{ from: string; to: string }>(() => ({
    from: dayjs().startOf('week').format(API_DATE_TIME),
    to: dayjs().endOf('week').format(API_DATE_TIME),
  }));

  const rooms = useQuery({
    queryKey: ['rooms', 'all'],
    queryFn: () => roomsApi.list({ page: 0, size: 200 }),
  });

  const buildings = useMemo(() => {
    const values = new Set(
      (rooms.data?.content ?? []).map((room) => room.building).filter((value): value is string => !!value),
    );
    return Array.from(values).sort();
  }, [rooms.data]);

  const occupancy = useQuery({
    queryKey: ['bookings', 'occupancy', roomId, building, minCapacity, range.from, range.to],
    queryFn: () =>
      roomId
        ? bookingsApi.occupancy(Number(roomId), range.from, range.to)
        : bookingsApi.occupancyAll({
            from: range.from,
            to: range.to,
            building,
            minCapacity: minCapacity === '' ? null : Number(minCapacity),
          }),
  });

  const events: EventInput[] = (occupancy.data ?? []).map((booking: Booking) => ({
    id: String(booking.id),
    title: `${booking.roomName ?? `#${booking.roomId}`} — ${booking.purpose ?? bookingStatusLabels[booking.status]}`,
    start: booking.startDateTime,
    end: booking.endDateTime,
    backgroundColor: STATUS_COLORS[booking.status],
    borderColor: STATUS_COLORS[booking.status],
  }));

  const handleDatesSet = (arg: DatesSetArg) => {
    const from = dayjs(arg.start).format(API_DATE_TIME);
    const to = dayjs(arg.end).format(API_DATE_TIME);
    setRange((current) => (current.from === from && current.to === to ? current : { from, to }));
  };

  return (
    <>
      <PageHeader title="Zauzetost prostorija" description="Kalendar rezervacija po salama (UC-SC-05)" />

      <Card withBorder radius="md" p="lg">
        <Group mb="md" wrap="wrap" align="flex-end">
          <Select
            label="Prostorija"
            placeholder="Sve prostorije"
            clearable
            searchable
            w={260}
            data={(rooms.data?.content ?? []).map((room) => ({
              value: String(room.id),
              label: `${room.code}${room.name ? ` — ${room.name}` : ''}`,
            }))}
            value={roomId}
            onChange={(value) => setSearchParams(value ? { roomId: value } : {})}
          />
          <Select
            label="Zgrada"
            placeholder="Sve zgrade"
            clearable
            w={200}
            data={buildings}
            value={building}
            onChange={setBuilding}
            disabled={!!roomId}
          />
          <NumberInput
            label="Min. kapacitet"
            placeholder="npr. 30"
            min={0}
            w={150}
            value={minCapacity}
            onChange={setMinCapacity}
            disabled={!!roomId}
          />
          <Group gap="xs" ml="auto">
            {(Object.keys(STATUS_COLORS) as BookingStatus[])
              .filter((status) => status !== 'REJECTED' && status !== 'CANCELLED')
              .map((status) => (
                <Badge key={status} variant="filled" style={{ backgroundColor: STATUS_COLORS[status] }}>
                  {bookingStatusLabels[status]}
                </Badge>
              ))}
          </Group>
        </Group>

        {roomId && (
          <Text size="xs" c="dimmed" mb="xs">
            Filteri po zgradi i kapacitetu važe samo kada je prikaz za sve prostorije.
          </Text>
        )}

        <QueryState isLoading={occupancy.isLoading} error={occupancy.error}>
          <FullCalendar
            plugins={[timeGridPlugin, dayGridPlugin]}
            initialView="timeGridWeek"
            locale={srLatnLocale}
            firstDay={1}
            height="auto"
            allDaySlot={false}
            slotMinTime="07:00:00"
            slotMaxTime="22:00:00"
            nowIndicator
            headerToolbar={{ left: 'prev,next today', center: 'title', right: 'timeGridWeek,timeGridDay,dayGridMonth' }}
            events={events}
            datesSet={handleDatesSet}
          />
        </QueryState>
      </Card>
    </>
  );
}
