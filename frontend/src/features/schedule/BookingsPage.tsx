import { useState } from 'react';
import { Badge, Button, Card, Group, SegmentedControl, Select, Table, Text } from '@mantine/core';
import { IconPlus, IconX } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { bookingsApi } from '../../api/schedule';
import { useAuth } from '../../auth/AuthContext';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { dash, formatInterval, formatHours } from '../../lib/format';
import { bookingStatusColors, bookingStatusLabels, teachingTypeLabels } from '../../lib/labels';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';
import type { BookingStatus } from '../../types/schedule';
import { BookingFormModal } from './BookingFormModal';

const STATUS_OPTIONS = (Object.keys(bookingStatusLabels) as BookingStatus[]).map((status) => ({
  value: status,
  label: bookingStatusLabels[status],
}));

/** Pregled rezervacija: svoje ili sve po statusu, sa otkazivanjem (UC-SC-04). */
export function BookingsPage() {
  const { user, isPrivileged } = useAuth();
  const workerId = user?.workerId ?? null;

  const [scope, setScope] = useState<'mine' | 'all'>(workerId ? 'mine' : 'all');
  const [status, setStatus] = useState<BookingStatus>('REQUESTED');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const queryClient = useQueryClient();

  const mine = scope === 'mine' && workerId != null;

  const query = useQuery({
    queryKey: ['bookings', 'list', mine ? `worker-${workerId}` : `status-${status}`, page, size],
    queryFn: () =>
      mine
        ? bookingsApi.byWorker(workerId!, { page, size, sort: 'startDateTime,desc' })
        : bookingsApi.byStatus(status, { page, size, sort: 'startDateTime,desc' }),
    placeholderData: keepPreviousData,
  });

  const cancel = useMutation({
    mutationFn: (id: number) => bookingsApi.cancel(id),
    onSuccess: () => {
      notifySuccess('Rezervacija je otkazana');
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
    },
    onError: (error) => notifyError(error, 'Otkazivanje nije uspelo'),
  });

  const cancellable = (bookingStatus: BookingStatus) =>
    bookingStatus === 'REQUESTED' || bookingStatus === 'ACCEPTED';

  return (
    <>
      <PageHeader
        title="Rezervacije"
        description="Rezervacije prostorija (UC-SC-02, UC-SC-04)"
        action={
          <Button leftSection={<IconPlus size={18} />} onClick={() => setFormOpen(true)}>
            Nova rezervacija
          </Button>
        }
      />

      <Card withBorder radius="md" p="lg">
        <Group mb="md" wrap="wrap">
          {workerId != null && (
            <SegmentedControl
              value={scope}
              onChange={(value) => {
                setScope(value as 'mine' | 'all');
                setPage(0);
              }}
              data={[
                { value: 'mine', label: 'Moje rezervacije' },
                { value: 'all', label: 'Sve rezervacije' },
              ]}
            />
          )}
          {!mine && (
            <Select
              w={200}
              data={STATUS_OPTIONS}
              value={status}
              allowDeselect={false}
              onChange={(value) => {
                if (value) {
                  setStatus(value as BookingStatus);
                  setPage(0);
                }
              }}
              aria-label="Status rezervacije"
            />
          )}
        </Group>

        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText={mine ? 'Nemate rezervacija' : 'Nema rezervacija sa izabranim statusom'}
        >
          <Table.ScrollContainer minWidth={800}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>Prostorija</Table.Th>
                  <Table.Th>Termin</Table.Th>
                  <Table.Th>Trajanje</Table.Th>
                  <Table.Th>Svrha</Table.Th>
                  <Table.Th>Tip</Table.Th>
                  <Table.Th>Status</Table.Th>
                  <Table.Th w={110} />
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {query.data?.content.map((booking) => (
                  <Table.Tr key={booking.id}>
                    <Table.Td>{booking.roomName ?? `#${booking.roomId}`}</Table.Td>
                    <Table.Td>{formatInterval(booking.startDateTime, booking.endDateTime)}</Table.Td>
                    <Table.Td>{formatHours(booking.durationHours)}</Table.Td>
                    <Table.Td>{dash(booking.purpose)}</Table.Td>
                    <Table.Td>
                      {booking.teachingType ? (
                        <Text size="sm">{teachingTypeLabels[booking.teachingType]}</Text>
                      ) : (
                        <Text size="sm" c="dimmed">
                          nenastavno
                        </Text>
                      )}
                    </Table.Td>
                    <Table.Td>
                      <Badge color={bookingStatusColors[booking.status]} variant="light">
                        {bookingStatusLabels[booking.status]}
                      </Badge>
                    </Table.Td>
                    <Table.Td>
                      {cancellable(booking.status) && (mine || isPrivileged) && (
                        <Button
                          size="compact-sm"
                          variant="subtle"
                          color="red"
                          leftSection={<IconX size={14} />}
                          onClick={() =>
                            confirmAction({
                              title: 'Otkazivanje rezervacije',
                              message: `Da li ste sigurni da želite da otkažete termin ${formatInterval(
                                booking.startDateTime,
                                booking.endDateTime,
                              )}?`,
                              confirmLabel: 'Otkaži',
                              onConfirm: () => cancel.mutate(booking.id),
                            })
                          }
                        >
                          Otkaži
                        </Button>
                      )}
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          </Table.ScrollContainer>

          <TablePagination
            data={query.data}
            page={page}
            size={size}
            onPageChange={setPage}
            onSizeChange={(value) => {
              setSize(value);
              setPage(0);
            }}
          />
        </QueryState>
      </Card>

      <BookingFormModal opened={formOpen} onClose={() => setFormOpen(false)} />
    </>
  );
}
