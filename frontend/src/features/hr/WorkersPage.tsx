import { useState } from 'react';
import { ActionIcon, Badge, Button, Card, Group, Table, TextInput, Tooltip } from '@mantine/core';
import { useDebouncedValue } from '@mantine/hooks';
import { IconEye, IconPlus, IconSearch, IconTrash } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { workersApi } from '../../api/hr';
import { useAuth } from '../../auth/AuthContext';
import { RoleGate } from '../../auth/RoleGate';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { employmentStatusColors, employmentStatusLabels } from '../../lib/labels';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';
import { WorkerFormModal } from './WorkerFormModal';

export function WorkersPage() {
  const [search, setSearch] = useState('');
  const [debouncedSearch] = useDebouncedValue(search, 300);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { isPrivileged } = useAuth();

  const query = useQuery({
    queryKey: ['workers', 'list', debouncedSearch, page, size],
    queryFn: () => workersApi.search(debouncedSearch, { page, size }),
    placeholderData: keepPreviousData,
  });

  const remove = useMutation({
    mutationFn: (id: number) => workersApi.remove(id),
    onSuccess: () => {
      notifySuccess('Zaposleni je obrisan');
      queryClient.invalidateQueries({ queryKey: ['workers'] });
    },
    onError: (error) => notifyError(error, 'Brisanje nije uspelo'),
  });

  return (
    <>
      <PageHeader
        title="Zaposleni"
        description="Evidencija zaposlenih (UC-HR-01)"
        action={
          isPrivileged && (
            <Button leftSection={<IconPlus size={18} />} onClick={() => setFormOpen(true)}>
              Novi zaposleni
            </Button>
          )
        }
      />

      <Card withBorder radius="md" p="lg">
        <TextInput
          placeholder="Pretraga po imenu, prezimenu, email-u ili JMBG-u"
          leftSection={<IconSearch size={16} />}
          value={search}
          onChange={(event) => {
            setSearch(event.currentTarget.value);
            setPage(0);
          }}
          mb="md"
          maw={460}
        />

        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText={debouncedSearch ? 'Nema rezultata za zadatu pretragu' : 'Još uvek nema zaposlenih'}
        >
          <Table.ScrollContainer minWidth={600}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th w={70}>ID</Table.Th>
                  <Table.Th>Ime i prezime</Table.Th>
                  <Table.Th>Email</Table.Th>
                  <Table.Th>Status</Table.Th>
                  <Table.Th w={100} />
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {query.data?.content.map((worker) => (
                  <Table.Tr key={worker.id} style={{ cursor: 'pointer' }}>
                    <Table.Td onClick={() => navigate(`/workers/${worker.id}`)}>{worker.id}</Table.Td>
                    <Table.Td onClick={() => navigate(`/workers/${worker.id}`)}>{worker.fullName}</Table.Td>
                    <Table.Td onClick={() => navigate(`/workers/${worker.id}`)}>{worker.email}</Table.Td>
                    <Table.Td onClick={() => navigate(`/workers/${worker.id}`)}>
                      <Badge color={employmentStatusColors[worker.employmentStatus]} variant="light">
                        {employmentStatusLabels[worker.employmentStatus]}
                      </Badge>
                    </Table.Td>
                    <Table.Td>
                      <Group gap={4} justify="flex-end" wrap="nowrap">
                        <Tooltip label="Profil">
                          <ActionIcon variant="subtle" onClick={() => navigate(`/workers/${worker.id}`)}>
                            <IconEye size={18} />
                          </ActionIcon>
                        </Tooltip>
                        <RoleGate roles={['ADMIN']}>
                          <Tooltip label="Obriši">
                            <ActionIcon
                              variant="subtle"
                              color="red"
                              onClick={() =>
                                confirmAction({
                                  title: 'Brisanje zaposlenog',
                                  message: `Da li ste sigurni da želite da obrišete zaposlenog „${worker.fullName}”?`,
                                  confirmLabel: 'Obriši',
                                  onConfirm: () => remove.mutate(worker.id),
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

      <WorkerFormModal opened={formOpen} onClose={() => setFormOpen(false)} />
    </>
  );
}
