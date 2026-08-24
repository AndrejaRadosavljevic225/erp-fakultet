import { Badge, Button, Group, Modal, Select, Stack, Table, Text } from '@mantine/core';
import { IconPlus, IconTrash } from '@tabler/icons-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { permissionsApi, rolesApi } from '../../api/hr';
import { QueryState } from '../../components/QueryState';
import { dash } from '../../lib/format';
import { notifyError, notifySuccess } from '../../lib/notify';
import type { Role } from '../../types/hr';

/** Dodela i uklanjanje permisija roli (UC-G-02). */
export function RolePermissionsModal({
  opened,
  onClose,
  role,
}: {
  opened: boolean;
  onClose: () => void;
  role: Role | null;
}) {
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<string | null>(null);

  const assigned = useQuery({
    queryKey: ['roles', 'permissions', role?.id],
    queryFn: () => rolesApi.permissions(role!.id),
    enabled: opened && !!role,
  });

  const all = useQuery({
    queryKey: ['permissions', 'all'],
    queryFn: () => permissionsApi.list({ page: 0, size: 200 }),
    enabled: opened,
  });

  const assignedIds = new Set((assigned.data ?? []).map((permission) => permission.id));
  const available = (all.data?.content ?? []).filter((permission) => !assignedIds.has(permission.id));

  const assign = useMutation({
    mutationFn: (permissionId: number) => rolesApi.assignPermission(role!.id, permissionId),
    onSuccess: () => {
      notifySuccess('Permisija je dodeljena');
      setSelected(null);
      queryClient.invalidateQueries({ queryKey: ['roles', 'permissions', role?.id] });
    },
    onError: (error) => notifyError(error, 'Dodela nije uspela'),
  });

  const remove = useMutation({
    mutationFn: (permissionId: number) => rolesApi.removePermission(role!.id, permissionId),
    onSuccess: () => {
      notifySuccess('Permisija je uklonjena');
      queryClient.invalidateQueries({ queryKey: ['roles', 'permissions', role?.id] });
    },
    onError: (error) => notifyError(error, 'Uklanjanje nije uspelo'),
  });

  return (
    <Modal opened={opened} onClose={onClose} title={role ? `Permisije role: ${role.name}` : 'Permisije role'} size="lg">
      <Stack>
        <Group align="flex-end">
          <Select
            label="Dodaj permisiju"
            placeholder="Izaberite permisiju"
            searchable
            flex={1}
            data={available.map((permission) => ({
              value: String(permission.id),
              label: `${permission.code} — ${permission.name}`,
            }))}
            value={selected}
            onChange={setSelected}
          />
          <Button
            leftSection={<IconPlus size={16} />}
            disabled={!selected}
            loading={assign.isPending}
            onClick={() => selected && assign.mutate(Number(selected))}
          >
            Dodaj
          </Button>
        </Group>

        <QueryState
          isLoading={assigned.isLoading}
          error={assigned.error}
          isEmpty={assigned.data?.length === 0}
          emptyText="Ovoj roli nije dodeljena nijedna permisija"
        >
          <Table>
            <Table.Thead>
              <Table.Tr>
                <Table.Th>Kod</Table.Th>
                <Table.Th>Naziv</Table.Th>
                <Table.Th>Modul</Table.Th>
                <Table.Th w={60} />
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {assigned.data?.map((permission) => (
                <Table.Tr key={permission.id}>
                  <Table.Td>
                    <Badge variant="light">{permission.code}</Badge>
                  </Table.Td>
                  <Table.Td>{permission.name}</Table.Td>
                  <Table.Td>{dash(permission.module)}</Table.Td>
                  <Table.Td>
                    <Button
                      size="compact-sm"
                      variant="subtle"
                      color="red"
                      leftSection={<IconTrash size={14} />}
                      onClick={() => remove.mutate(permission.id)}
                    >
                      Ukloni
                    </Button>
                  </Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>
        </QueryState>

        <Text size="xs" c="dimmed">
          Napomena: pristup ekranima se trenutno određuje kodom role (ADMIN / HR / PROFESOR). Permisije su dodatni,
          finiji sloj koji backend evidentira uz rolu.
        </Text>
      </Stack>
    </Modal>
  );
}
