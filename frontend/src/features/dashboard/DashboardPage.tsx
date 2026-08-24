import { Badge, Card, Group, SimpleGrid, Stack, Table, Text, ThemeIcon, Title } from '@mantine/core';
import {
  IconBuilding,
  IconCalendarEvent,
  IconChecklist,
  IconUserCog,
  IconUsers,
} from '@tabler/icons-react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { bookingsApi, roomsApi } from '../../api/schedule';
import { usersApi, workersApi } from '../../api/hr';
import { useAuth } from '../../auth/AuthContext';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { bookingStatusColors, bookingStatusLabels } from '../../lib/labels';
import { formatInterval } from '../../lib/format';

function StatCard({
  label,
  value,
  icon,
  color,
  to,
}: {
  label: string;
  value: number | string;
  icon: React.ReactNode;
  color: string;
  to: string;
}) {
  return (
    <Card withBorder radius="md" p="lg" component={Link} to={to} style={{ textDecoration: 'none' }}>
      <Group justify="space-between" wrap="nowrap">
        <Stack gap={2}>
          <Text size="xs" c="dimmed" tt="uppercase" fw={700}>
            {label}
          </Text>
          <Text size="xl" fw={700}>
            {value}
          </Text>
        </Stack>
        <ThemeIcon color={color} variant="light" size={44} radius="md">
          {icon}
        </ThemeIcon>
      </Group>
    </Card>
  );
}

export function DashboardPage() {
  const { user, isPrivileged } = useAuth();
  const workerId = user?.workerId ?? null;

  const workers = useQuery({
    queryKey: ['workers', 'count'],
    queryFn: () => workersApi.search(undefined, { page: 0, size: 1 }),
  });

  const rooms = useQuery({
    queryKey: ['rooms', 'count'],
    queryFn: () => roomsApi.list({ page: 0, size: 1 }),
  });

  const users = useQuery({
    queryKey: ['users', 'count'],
    queryFn: () => usersApi.list({ page: 0, size: 1 }),
    enabled: isPrivileged,
  });

  const pending = useQuery({
    queryKey: ['bookings', 'REQUESTED', 'count'],
    queryFn: () => bookingsApi.byStatus('REQUESTED', { page: 0, size: 1 }),
    enabled: isPrivileged,
  });

  const myBookings = useQuery({
    queryKey: ['bookings', 'worker', workerId],
    queryFn: () => bookingsApi.byWorker(workerId!, { page: 0, size: 5, sort: 'startDateTime,desc' }),
    enabled: workerId != null,
  });

  return (
    <>
      <PageHeader
        title={`Dobrodošli, ${user?.workerFullName ?? user?.username ?? ''}`}
        description="Pregled stanja sistema"
      />

      <SimpleGrid cols={{ base: 1, sm: 2, lg: 4 }} mb="xl">
        <StatCard
          label="Zaposleni"
          value={workers.data?.totalElements ?? '—'}
          icon={<IconUsers size={22} />}
          color="blue"
          to="/workers"
        />
        <StatCard
          label="Prostorije"
          value={rooms.data?.totalElements ?? '—'}
          icon={<IconBuilding size={22} />}
          color="grape"
          to="/rooms"
        />
        {isPrivileged && (
          <StatCard
            label="Korisnički nalozi"
            value={users.data?.totalElements ?? '—'}
            icon={<IconUserCog size={22} />}
            color="teal"
            to="/users"
          />
        )}
        {isPrivileged && (
          <StatCard
            label="Čeka odobrenje"
            value={pending.data?.totalElements ?? '—'}
            icon={<IconChecklist size={22} />}
            color="orange"
            to="/approvals"
          />
        )}
      </SimpleGrid>

      {workerId != null && (
        <Card withBorder radius="md" p="lg">
          <Group gap="xs" mb="md">
            <IconCalendarEvent size={20} />
            <Title order={4}>Moje poslednje rezervacije</Title>
          </Group>
          <QueryState
            isLoading={myBookings.isLoading}
            error={myBookings.error}
            isEmpty={myBookings.data?.content.length === 0}
            emptyText="Još uvek nemate rezervacija"
          >
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>Prostorija</Table.Th>
                  <Table.Th>Termin</Table.Th>
                  <Table.Th>Svrha</Table.Th>
                  <Table.Th>Status</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {myBookings.data?.content.map((booking) => (
                  <Table.Tr key={booking.id}>
                    <Table.Td>{booking.roomName ?? `#${booking.roomId}`}</Table.Td>
                    <Table.Td>{formatInterval(booking.startDateTime, booking.endDateTime)}</Table.Td>
                    <Table.Td>{booking.purpose ?? '—'}</Table.Td>
                    <Table.Td>
                      <Badge color={bookingStatusColors[booking.status]} variant="light">
                        {bookingStatusLabels[booking.status]}
                      </Badge>
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          </QueryState>
        </Card>
      )}
    </>
  );
}
