import { useState } from 'react';
import {
  ActionIcon,
  Badge,
  Button,
  Card,
  Group,
  Modal,
  Stack,
  Switch,
  Table,
  TextInput,
  Textarea,
  Tooltip,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconKey, IconPencil, IconPlus, IconTrash } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { rolesApi } from '../../api/hr';
import { fieldErrors } from '../../api/client';
import { useAuth } from '../../auth/AuthContext';
import { RoleGate } from '../../auth/RoleGate';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { dash } from '../../lib/format';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';
import type { Role } from '../../types/hr';
import { RolePermissionsModal } from './RolePermissionsModal';

interface FormValues {
  code: string;
  name: string;
  description: string;
  isActive: boolean;
}

const EMPTY: FormValues = { code: '', name: '', description: '', isActive: true };

export function RolesPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Role | null>(null);
  const [permissionsRole, setPermissionsRole] = useState<Role | null>(null);
  const queryClient = useQueryClient();
  const { isAdmin } = useAuth();

  const query = useQuery({
    queryKey: ['roles', 'list', page, size],
    queryFn: () => rolesApi.list({ page, size }),
    placeholderData: keepPreviousData,
  });

  const form = useForm<FormValues>({
    initialValues: EMPTY,
    validate: {
      code: (value) => (editing || value.trim() ? null : 'Kod role je obavezan'),
      name: (value) => (value.trim() ? null : 'Naziv role je obavezan'),
    },
  });

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      editing
        ? rolesApi.update(editing.id, {
            name: values.name.trim(),
            description: values.description.trim() || null,
            isActive: values.isActive,
          })
        : rolesApi.create({
            code: values.code.trim().toUpperCase(),
            name: values.name.trim(),
            description: values.description.trim() || null,
          }),
    onSuccess: () => {
      notifySuccess(editing ? 'Rola je izmenjena' : 'Rola je kreirana');
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      setFormOpen(false);
    },
    onError: (error) => {
      const errors = fieldErrors(error);
      if (Object.keys(errors).length) form.setErrors(errors);
      else notifyError(error, 'Čuvanje nije uspelo');
    },
  });

  const remove = useMutation({
    mutationFn: (id: number) => rolesApi.remove(id),
    onSuccess: () => {
      notifySuccess('Rola je obrisana');
      queryClient.invalidateQueries({ queryKey: ['roles'] });
    },
    onError: (error) => notifyError(error, 'Brisanje nije uspelo'),
  });

  return (
    <>
      <PageHeader
        title="Role"
        description="Role korisnika i njihove permisije (UC-G-02)"
        action={
          isAdmin && (
            <Button
              leftSection={<IconPlus size={18} />}
              onClick={() => {
                setEditing(null);
                form.setValues(EMPTY);
                setFormOpen(true);
              }}
            >
              Nova rola
            </Button>
          )
        }
      />

      <Card withBorder radius="md" p="lg">
        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText="Nema definisanih rola"
        >
          <Table.ScrollContainer minWidth={700}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th w={70}>ID</Table.Th>
                  <Table.Th>Kod</Table.Th>
                  <Table.Th>Naziv</Table.Th>
                  <Table.Th>Opis</Table.Th>
                  <Table.Th>Status</Table.Th>
                  <Table.Th w={130} />
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {query.data?.content.map((role) => (
                  <Table.Tr key={role.id}>
                    <Table.Td>{role.id}</Table.Td>
                    <Table.Td>
                      <Badge variant="light">{role.code}</Badge>
                    </Table.Td>
                    <Table.Td>{role.name}</Table.Td>
                    <Table.Td>{dash(role.description)}</Table.Td>
                    <Table.Td>
                      <Badge color={role.active ? 'green' : 'gray'} variant="light">
                        {role.active ? 'Aktivna' : 'Neaktivna'}
                      </Badge>
                    </Table.Td>
                    <Table.Td>
                      <Group gap={4} justify="flex-end" wrap="nowrap">
                        <Tooltip label="Permisije">
                          <ActionIcon variant="subtle" onClick={() => setPermissionsRole(role)}>
                            <IconKey size={18} />
                          </ActionIcon>
                        </Tooltip>
                        <RoleGate roles={['ADMIN']}>
                          <Tooltip label="Izmeni">
                            <ActionIcon
                              variant="subtle"
                              onClick={() => {
                                setEditing(role);
                                form.setValues({
                                  code: role.code,
                                  name: role.name,
                                  description: role.description ?? '',
                                  isActive: role.active,
                                });
                                setFormOpen(true);
                              }}
                            >
                              <IconPencil size={18} />
                            </ActionIcon>
                          </Tooltip>
                          <Tooltip label="Obriši">
                            <ActionIcon
                              variant="subtle"
                              color="red"
                              onClick={() =>
                                confirmAction({
                                  title: 'Brisanje role',
                                  message: `Da li ste sigurni da želite da obrišete rolu „${role.name}”?`,
                                  confirmLabel: 'Obriši',
                                  onConfirm: () => remove.mutate(role.id),
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

      <Modal opened={formOpen} onClose={() => setFormOpen(false)} title={editing ? 'Izmena role' : 'Nova rola'}>
        <form onSubmit={form.onSubmit((values) => mutation.mutate(values))}>
          <Stack>
            <TextInput
              label="Kod"
              placeholder="npr. DEKAN"
              withAsterisk
              disabled={!!editing}
              {...form.getInputProps('code')}
            />
            <TextInput label="Naziv" withAsterisk {...form.getInputProps('name')} />
            <Textarea label="Opis" autosize minRows={2} {...form.getInputProps('description')} />
            {editing && <Switch label="Rola je aktivna" {...form.getInputProps('isActive', { type: 'checkbox' })} />}
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

      <RolePermissionsModal
        opened={!!permissionsRole}
        onClose={() => setPermissionsRole(null)}
        role={permissionsRole}
      />
    </>
  );
}
