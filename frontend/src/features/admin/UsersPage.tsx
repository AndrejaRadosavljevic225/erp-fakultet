import { useState } from 'react';
import {
  ActionIcon,
  Badge,
  Button,
  Card,
  Group,
  Modal,
  PasswordInput,
  Select,
  Stack,
  Switch,
  Table,
  TextInput,
  Tooltip,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconPencil, IconPlus, IconTrash } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { rolesApi, usersApi, workersApi } from '../../api/hr';
import { fieldErrors } from '../../api/client';
import { RoleGate } from '../../auth/RoleGate';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { dash, formatDateTime } from '../../lib/format';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';
import type { User } from '../../types/hr';

interface FormValues {
  username: string;
  password: string;
  workerId: string | null;
  roleId: string | null;
  isActive: boolean;
}

const EMPTY: FormValues = { username: '', password: '', workerId: null, roleId: null, isActive: true };

/** Korisnicki nalozi zaposlenih: kreiranje naloga i dodela role (UC-HR-02, UC-G-02). */
export function UsersPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<User | null>(null);
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: ['users', 'list', page, size],
    queryFn: () => usersApi.list({ page, size }),
    placeholderData: keepPreviousData,
  });

  // Ucitava se odmah, jer se imena zaposlenih prikazuju i u koloni tabele, ne samo u formi.
  const workers = useQuery({
    queryKey: ['workers', 'all'],
    queryFn: () => workersApi.search(undefined, { page: 0, size: 200 }),
  });

  const roles = useQuery({
    queryKey: ['roles', 'all'],
    queryFn: () => rolesApi.list({ page: 0, size: 100 }),
  });

  const roleName = (id: number | null) =>
    id == null ? '—' : (roles.data?.content.find((role) => role.id === id)?.name ?? `#${id}`);

  const workerName = (id: number | null) =>
    id == null ? '—' : (workers.data?.content.find((worker) => worker.id === id)?.fullName ?? `#${id}`);

  const form = useForm<FormValues>({
    initialValues: EMPTY,
    validate: {
      username: (value) =>
        editing || value.trim().length >= 3 ? null : 'Korisničko ime mora imati bar 3 karaktera',
      password: (value) => (editing || value.length >= 6 ? null : 'Lozinka mora imati bar 6 karaktera'),
    },
  });

  const openCreate = () => {
    setEditing(null);
    form.setValues(EMPTY);
    setFormOpen(true);
  };

  const openEdit = (user: User) => {
    setEditing(user);
    form.setValues({
      username: user.username,
      password: '',
      workerId: user.workerId ? String(user.workerId) : null,
      roleId: user.roleId ? String(user.roleId) : null,
      isActive: user.active,
    });
    setFormOpen(true);
  };

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const common = {
        workerId: values.workerId ? Number(values.workerId) : null,
        roleId: values.roleId ? Number(values.roleId) : null,
        isActive: values.isActive,
      };
      return editing
        ? usersApi.update(editing.id, common)
        : usersApi.create({ ...common, username: values.username.trim(), password: values.password });
    },
    onSuccess: () => {
      notifySuccess(editing ? 'Nalog je izmenjen' : 'Nalog je kreiran');
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setFormOpen(false);
    },
    onError: (error) => {
      const errors = fieldErrors(error);
      if (Object.keys(errors).length) form.setErrors(errors);
      else notifyError(error, 'Čuvanje nije uspelo');
    },
  });

  const remove = useMutation({
    mutationFn: (id: number) => usersApi.remove(id),
    onSuccess: () => {
      notifySuccess('Nalog je obrisan');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (error) => notifyError(error, 'Brisanje nije uspelo'),
  });

  return (
    <>
      <PageHeader
        title="Korisnički nalozi"
        description="Nalozi zaposlenih i njihove role (UC-HR-02)"
        action={
          <Button leftSection={<IconPlus size={18} />} onClick={openCreate}>
            Novi nalog
          </Button>
        }
      />

      <Card withBorder radius="md" p="lg">
        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText="Još uvek nema korisničkih naloga"
        >
          <Table.ScrollContainer minWidth={760}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th w={70}>ID</Table.Th>
                  <Table.Th>Korisničko ime</Table.Th>
                  <Table.Th>Zaposleni</Table.Th>
                  <Table.Th>Rola</Table.Th>
                  <Table.Th>Poslednja prijava</Table.Th>
                  <Table.Th>Status</Table.Th>
                  <Table.Th w={100} />
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {query.data?.content.map((user) => (
                  <Table.Tr key={user.id}>
                    <Table.Td>{user.id}</Table.Td>
                    <Table.Td fw={500}>{user.username}</Table.Td>
                    <Table.Td>{user.workerId ? workerName(user.workerId) : '—'}</Table.Td>
                    <Table.Td>{dash(user.roleName ?? roleName(user.roleId))}</Table.Td>
                    <Table.Td>{formatDateTime(user.lastLogin)}</Table.Td>
                    <Table.Td>
                      <Badge color={user.active ? 'green' : 'gray'} variant="light">
                        {user.active ? 'Aktivan' : 'Deaktiviran'}
                      </Badge>
                    </Table.Td>
                    <Table.Td>
                      <Group gap={4} justify="flex-end" wrap="nowrap">
                        <Tooltip label="Izmeni">
                          <ActionIcon variant="subtle" onClick={() => openEdit(user)}>
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
                                  title: 'Brisanje naloga',
                                  message: `Da li ste sigurni da želite da obrišete nalog „${user.username}”?`,
                                  confirmLabel: 'Obriši',
                                  onConfirm: () => remove.mutate(user.id),
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

      <Modal
        opened={formOpen}
        onClose={() => setFormOpen(false)}
        title={editing ? `Izmena naloga: ${editing.username}` : 'Novi korisnički nalog'}
      >
        <form onSubmit={form.onSubmit((values) => mutation.mutate(values))}>
          <Stack>
            <TextInput label="Korisničko ime" withAsterisk disabled={!!editing} {...form.getInputProps('username')} />
            {!editing && (
              <PasswordInput
                label="Početna lozinka"
                description="Korisnik je menja kroz „Promena lozinke”"
                withAsterisk
                {...form.getInputProps('password')}
              />
            )}
            <Select
              label="Zaposleni"
              placeholder="Bez povezanog zaposlenog"
              searchable
              clearable
              data={(workers.data?.content ?? []).map((worker) => ({
                value: String(worker.id),
                label: worker.fullName,
              }))}
              {...form.getInputProps('workerId')}
            />
            <Select
              label="Rola"
              placeholder="Bez role"
              clearable
              data={(roles.data?.content ?? []).map((role) => ({
                value: String(role.id),
                label: `${role.name} (${role.code})`,
              }))}
              {...form.getInputProps('roleId')}
            />
            <Switch label="Nalog je aktivan" {...form.getInputProps('isActive', { type: 'checkbox' })} />
            <Group justify="flex-end">
              <Button variant="default" onClick={() => setFormOpen(false)}>
                Odustani
              </Button>
              <Button type="submit" loading={mutation.isPending}>
                Sačuvaj
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>
    </>
  );
}
