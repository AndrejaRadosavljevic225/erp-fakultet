import { useState } from 'react';
import { ActionIcon, Badge, Button, Card, Group, Table, Tooltip } from '@mantine/core';
import { IconPencil, IconPlus, IconTrash } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { positionsApi } from '../../api/hr';
import { RoleGate } from '../../auth/RoleGate';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { dash, formatMoney } from '../../lib/format';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';
import type { Position } from '../../types/hr';
import { PositionFormModal } from './PositionFormModal';

export function PositionsPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Position | null>(null);
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: ['positions', 'list', page, size],
    queryFn: () => positionsApi.list({ page, size }),
    placeholderData: keepPreviousData,
  });

  const remove = useMutation({
    mutationFn: (id: number) => positionsApi.remove(id),
    onSuccess: () => {
      notifySuccess('Radno mesto je obrisano');
      queryClient.invalidateQueries({ queryKey: ['positions'] });
    },
    onError: (error) => notifyError(error, 'Brisanje nije uspelo'),
  });

  const openCreate = () => {
    setEditing(null);
    setFormOpen(true);
  };

  return (
    <>
      <PageHeader
        title="Radna mesta"
        description="Organizaciona struktura i pozicije (UC-HR-05)"
        action={
          <Button leftSection={<IconPlus size={18} />} onClick={openCreate}>
            Novo radno mesto
          </Button>
        }
      />

      <Card withBorder radius="md" p="lg">
        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText="Još uvek nema definisanih radnih mesta"
        >
          <Table.ScrollContainer minWidth={600}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th w={70}>ID</Table.Th>
                  <Table.Th>Naziv</Table.Th>
                  <Table.Th>Platni razred</Table.Th>
                  <Table.Th>Osnovna plata</Table.Th>
                  <Table.Th>Popunjenost</Table.Th>
                  <Table.Th w={100} />
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {query.data?.content.map((position) => (
                  <Table.Tr key={position.id}>
                    <Table.Td>{position.id}</Table.Td>
                    <Table.Td>{position.title}</Table.Td>
                    <Table.Td>{dash(position.salaryGrade)}</Table.Td>
                    <Table.Td>{formatMoney(position.baseSalary)}</Table.Td>
                    <Table.Td>
                      <Badge color={position.vacant ? 'yellow' : 'green'} variant="light">
                        {position.vacant ? 'Slobodno' : 'Popunjeno'}
                      </Badge>
                    </Table.Td>
                    <Table.Td>
                      <Group gap={4} justify="flex-end" wrap="nowrap">
                        <Tooltip label="Izmeni">
                          <ActionIcon
                            variant="subtle"
                            onClick={() => {
                              setEditing(position);
                              setFormOpen(true);
                            }}
                          >
                            <IconPencil size={18} />
                          </ActionIcon>
                        </Tooltip>
                        <RoleGate roles={['ADMIN']}>
                          <Tooltip label="Obriši">
                            <ActionIcon
                              variant="subtle"
                              color="red"
                              onClick={() =>
                                confirmAction({
                                  title: 'Brisanje radnog mesta',
                                  message: `Da li ste sigurni da želite da obrišete „${position.title}”?`,
                                  confirmLabel: 'Obriši',
                                  onConfirm: () => remove.mutate(position.id),
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

      <PositionFormModal opened={formOpen} onClose={() => setFormOpen(false)} position={editing} />
    </>
  );
}
