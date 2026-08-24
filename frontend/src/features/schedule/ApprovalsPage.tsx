import { useState } from 'react';
import { Alert, Badge, Button, Card, Group, Table, Text } from '@mantine/core';
import { IconCheck, IconInfoCircle, IconX } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { bookingsApi } from '../../api/schedule';
import { useAuth } from '../../auth/AuthContext';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { dash, formatHours, formatInterval } from '../../lib/format';
import { teachingTypeLabels } from '../../lib/labels';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';

/** Pregled i obrada rezervacija koje cekaju odobrenje (UC-SC-03). */
export function ApprovalsPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const queryClient = useQueryClient();
  const { user } = useAuth();

  const query = useQuery({
    queryKey: ['bookings', 'list', 'status-REQUESTED', page, size],
    queryFn: () => bookingsApi.byStatus('REQUESTED', { page, size, sort: 'startDateTime,asc' }),
    placeholderData: keepPreviousData,
  });

  const decide = useMutation({
    mutationFn: ({ id, approve }: { id: number; approve: boolean }) =>
      approve ? bookingsApi.approve(id, user?.workerId) : bookingsApi.reject(id, user?.workerId),
    onSuccess: (_data, variables) => {
      notifySuccess(variables.approve ? 'Rezervacija je odobrena' : 'Rezervacija je odbijena');
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
    },
    onError: (error) => notifyError(error, 'Akcija nije uspela'),
  });

  return (
    <>
      <PageHeader title="Odobravanje rezervacija" description="Zahtevi koji čekaju odluku (UC-SC-03)" />

      <Alert color="blue" icon={<IconInfoCircle size={18} />} mb="md">
        Sistem trenutno ne šalje email obaveštenja — podnosilac status vidi u svojoj listi rezervacija.
      </Alert>

      <Card withBorder radius="md" p="lg">
        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText="Nema zahteva koji čekaju odobrenje"
        >
          <Table.ScrollContainer minWidth={800}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>Prostorija</Table.Th>
                  <Table.Th>Termin</Table.Th>
                  <Table.Th>Trajanje</Table.Th>
                  <Table.Th>Podnosilac</Table.Th>
                  <Table.Th>Svrha</Table.Th>
                  <Table.Th>Tip</Table.Th>
                  <Table.Th w={190} />
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {query.data?.content.map((booking) => (
                  <Table.Tr key={booking.id}>
                    <Table.Td>{booking.roomName ?? `#${booking.roomId}`}</Table.Td>
                    <Table.Td>{formatInterval(booking.startDateTime, booking.endDateTime)}</Table.Td>
                    <Table.Td>{formatHours(booking.durationHours)}</Table.Td>
                    <Table.Td>
                      <Text size="sm">zaposleni #{booking.requesterWorkerId}</Text>
                    </Table.Td>
                    <Table.Td>{dash(booking.purpose)}</Table.Td>
                    <Table.Td>
                      {booking.teachingType ? (
                        <Badge variant="light" size="sm">
                          {teachingTypeLabels[booking.teachingType]}
                        </Badge>
                      ) : (
                        <Text size="sm" c="dimmed">
                          nenastavno
                        </Text>
                      )}
                    </Table.Td>
                    <Table.Td>
                      <Group gap={6} justify="flex-end" wrap="nowrap">
                        <Button
                          size="compact-sm"
                          color="green"
                          leftSection={<IconCheck size={14} />}
                          loading={decide.isPending && decide.variables?.id === booking.id}
                          onClick={() => decide.mutate({ id: booking.id, approve: true })}
                        >
                          Odobri
                        </Button>
                        <Button
                          size="compact-sm"
                          color="red"
                          variant="light"
                          leftSection={<IconX size={14} />}
                          onClick={() =>
                            confirmAction({
                              title: 'Odbijanje rezervacije',
                              message: 'Da li ste sigurni da želite da odbijete ovaj zahtev?',
                              confirmLabel: 'Odbij',
                              onConfirm: () => decide.mutate({ id: booking.id, approve: false }),
                            })
                          }
                        >
                          Odbij
                        </Button>
                      </Group>
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
    </>
  );
}
