import { useState } from 'react';
import { ActionIcon, Badge, Button, Card, Group, Table, Tooltip } from '@mantine/core';
import { IconCalendarPlus, IconPencil, IconPlus, IconTrash } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { roomsApi } from '../../api/schedule';
import { useAuth } from '../../auth/AuthContext';
import { RoleGate } from '../../auth/RoleGate';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { dash } from '../../lib/format';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';
import type { Room } from '../../types/schedule';
import { RoomFormModal } from './RoomFormModal';

export function RoomsPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Room | null>(null);
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { isPrivileged } = useAuth();

  const query = useQuery({
    queryKey: ['rooms', 'list', page, size],
    queryFn: () => roomsApi.list({ page, size }),
    placeholderData: keepPreviousData,
  });

  const remove = useMutation({
    mutationFn: (id: number) => roomsApi.remove(id),
    onSuccess: () => {
      notifySuccess('Prostorija je obrisana');
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
    },
    onError: (error) => notifyError(error, 'Brisanje nije uspelo'),
  });

  return (
    <>
      <PageHeader
        title="Prostorije"
        description="Sale, amfiteatri i laboratorije (UC-SC-01)"
        action={
          isPrivileged && (
            <Button
              leftSection={<IconPlus size={18} />}
              onClick={() => {
                setEditing(null);
                setFormOpen(true);
              }}
            >
              Nova prostorija
            </Button>
          )
        }
      />

      <Card withBorder radius="md" p="lg">
        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText="Još uvek nema unetih prostorija"
        >
          <Table.ScrollContainer minWidth={700}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>Oznaka</Table.Th>
                  <Table.Th>Naziv</Table.Th>
                  <Table.Th>Zgrada</Table.Th>
                  <Table.Th>Sprat</Table.Th>
                  <Table.Th>Kapacitet</Table.Th>
                  <Table.Th>Status</Table.Th>
                  <Table.Th w={120} />
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {query.data?.content.map((room) => (
                  <Table.Tr key={room.id}>
                    <Table.Td fw={500}>{room.code}</Table.Td>
                    <Table.Td>{dash(room.name)}</Table.Td>
                    <Table.Td>{dash(room.building)}</Table.Td>
                    <Table.Td>{dash(room.floor)}</Table.Td>
                    <Table.Td>{dash(room.capacity)}</Table.Td>
                    <Table.Td>
                      <Group gap={4}>
                        {!room.active && (
                          <Badge color="gray" variant="light">
                            Neaktivna
                          </Badge>
                        )}
                        <Badge color={room.bookable ? 'green' : 'gray'} variant="light">
                          {room.bookable ? 'Rezervabilna' : 'Nije za rezervaciju'}
                        </Badge>
                      </Group>
                    </Table.Td>
                    <Table.Td>
                      <Group gap={4} justify="flex-end" wrap="nowrap">
                        <Tooltip label="Zauzetost">
                          <ActionIcon variant="subtle" onClick={() => navigate(`/calendar?roomId=${room.id}`)}>
                            <IconCalendarPlus size={18} />
                          </ActionIcon>
                        </Tooltip>
                        {isPrivileged && (
                          <Tooltip label="Izmeni">
                            <ActionIcon
                              variant="subtle"
                              onClick={() => {
                                setEditing(room);
                                setFormOpen(true);
                              }}
                            >
                              <IconPencil size={18} />
                            </ActionIcon>
                          </Tooltip>
                        )}
                        <RoleGate roles={['ADMIN']}>
                          <Tooltip label="Obriši">
                            <ActionIcon
                              variant="subtle"
                              color="red"
                              onClick={() =>
                                confirmAction({
                                  title: 'Brisanje prostorije',
                                  message: `Da li ste sigurni da želite da obrišete prostoriju „${room.code}”?`,
                                  confirmLabel: 'Obriši',
                                  onConfirm: () => remove.mutate(room.id),
                                })
                              }
                            >
                              <IconTrash size={18} />
                            </ActionIcon>
                          </Tooltip>
                        </RoleGate>
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

      <RoomFormModal opened={formOpen} onClose={() => setFormOpen(false)} room={editing} />
    </>
  );
}
