import { useState } from 'react';
import { ActionIcon, Badge, Button, Card, Group, Modal, Stack, Table, TextInput, Tooltip } from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconPlus, IconTrash } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { permissionsApi } from '../../api/hr';
import { fieldErrors } from '../../api/client';
import { useAuth } from '../../auth/AuthContext';
import { RoleGate } from '../../auth/RoleGate';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { dash } from '../../lib/format';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';

export function PermissionsPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const queryClient = useQueryClient();
  const { isAdmin } = useAuth();

  const query = useQuery({
    queryKey: ['permissions', 'list', page, size],
    queryFn: () => permissionsApi.list({ page, size }),
    placeholderData: keepPreviousData,
  });

  const form = useForm({
    initialValues: { code: '', name: '', module: '' },
    validate: {
      code: (value) => (value.trim() ? null : 'Kod permisije je obavezan'),
      name: (value) => (value.trim() ? null : 'Naziv permisije je obavezan'),
    },
  });

  const create = useMutation({
    mutationFn: (values: typeof form.values) =>
      permissionsApi.create({
        code: values.code.trim().toUpperCase(),
        name: values.name.trim(),
        module: values.module.trim() || null,
      }),
    onSuccess: () => {
      notifySuccess('Permisija je kreirana');
      queryClient.invalidateQueries({ queryKey: ['permissions'] });
      setFormOpen(false);
      form.reset();
    },
    onError: (error) => {
      const errors = fieldErrors(error);
      if (Object.keys(errors).length) form.setErrors(errors);
      else notifyError(error, 'Čuvanje nije uspelo');
    },
  });

  const remove = useMutation({
    mutationFn: (id: number) => permissionsApi.remove(id),
    onSuccess: () => {
      notifySuccess('Permisija je obrisana');
      queryClient.invalidateQueries({ queryKey: ['permissions'] });
    },
    onError: (error) => notifyError(error, 'Brisanje nije uspelo'),
  });

  return (
    <>
      <PageHeader
        title="Permisije"
        description="Kodovi permisija koji se dodeljuju rolama (UC-G-02)"
        action={
          isAdmin && (
            <Button leftSection={<IconPlus size={18} />} onClick={() => setFormOpen(true)}>
              Nova permisija
            </Button>
          )
        }
      />

      <Card withBorder radius="md" p="lg">
        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText="Nema definisanih permisija"
        >
          <Table highlightOnHover>
            <Table.Thead>
              <Table.Tr>
                <Table.Th w={70}>ID</Table.Th>
                <Table.Th>Kod</Table.Th>
                <Table.Th>Naziv</Table.Th>
                <Table.Th>Modul</Table.Th>
                <Table.Th w={60} />
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {query.data?.content.map((permission) => (
                <Table.Tr key={permission.id}>
                  <Table.Td>{permission.id}</Table.Td>
                  <Table.Td>
                    <Badge variant="light">{permission.code}</Badge>
                  </Table.Td>
                  <Table.Td>{permission.name}</Table.Td>
                  <Table.Td>{dash(permission.module)}</Table.Td>
                  <Table.Td>
                    <RoleGate roles={['ADMIN']}>
                      <Tooltip label="Obriši">
                        <ActionIcon
                          variant="subtle"
                          color="red"
                          onClick={() =>
                            confirmAction({
                              title: 'Brisanje permisije',
                              message: `Da li ste sigurni da želite da obrišete permisiju „${permission.code}”?`,
                              confirmLabel: 'Obriši',
                              onConfirm: () => remove.mutate(permission.id),
                            })
                          }
                        >
                          <IconTrash size={18} />
                        </ActionIcon>
                      </Tooltip>
                    </RoleGate>
                  </Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>

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

      <Modal opened={formOpen} onClose={() => setFormOpen(false)} title="Nova permisija">
        <form onSubmit={form.onSubmit((values) => create.mutate(values))}>
          <Stack>
            <TextInput label="Kod" placeholder="npr. WORKER_WRITE" withAsterisk {...form.getInputProps('code')} />
            <TextInput label="Naziv" withAsterisk {...form.getInputProps('name')} />
            <TextInput label="Modul" placeholder="npr. HR, SCHEDULE" {...form.getInputProps('module')} />
            <Group justify="flex-end">
              <Button variant="default" onClick={() => setFormOpen(false)}>
                Odustani
              </Button>
              <Button type="submit" loading={create.isPending}>
                Sačuvaj
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>
    </>
  );
}
