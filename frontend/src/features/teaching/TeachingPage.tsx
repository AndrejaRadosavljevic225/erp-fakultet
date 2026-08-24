import { useState } from 'react';
import {
  ActionIcon,
  Button,
  Card,
  Group,
  Modal,
  NumberInput,
  Select,
  Stack,
  Table,
  Tabs,
  Textarea,
  Tooltip,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconChartBar, IconListNumbers, IconPlus, IconTrash, IconUsers } from '@tabler/icons-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { rolesApi, workersApi } from '../../api/hr';
import { schoolYearsApi, teachingApi } from '../../api/schedule';
import { useAuth } from '../../auth/AuthContext';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { dash, formatHours } from '../../lib/format';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';
import { TeachingReportPanel } from './TeachingReportPanel';

export function TeachingPage() {
  const { isPrivileged } = useAuth();
  const queryClient = useQueryClient();
  const [normOpen, setNormOpen] = useState(false);
  const [assignOpen, setAssignOpen] = useState(false);
  const [assignmentYearId, setAssignmentYearId] = useState<string | null>(null);

  const schoolYears = useQuery({
    queryKey: ['school-years', 'all'],
    queryFn: () => schoolYearsApi.list({ page: 0, size: 100 }),
  });

  const years = schoolYears.data?.content ?? [];
  const yearOptions = years.map((year) => ({ value: String(year.id), label: year.code }));
  const yearCode = (id: number) => years.find((year) => year.id === id)?.code ?? `#${id}`;

  const roles = useQuery({
    queryKey: ['roles', 'all'],
    queryFn: () => rolesApi.list({ page: 0, size: 100 }),
    enabled: isPrivileged,
  });

  const roleName = (id: number | null) =>
    id == null ? '—' : (roles.data?.content.find((role) => role.id === id)?.name ?? `#${id}`);

  const workers = useQuery({
    queryKey: ['workers', 'all'],
    queryFn: () => workersApi.search(undefined, { page: 0, size: 200 }),
    enabled: isPrivileged,
  });

  const workerName = (id: number) =>
    workers.data?.content.find((worker) => worker.id === id)?.fullName ?? `zaposleni #${id}`;

  const norms = useQuery({
    queryKey: ['teaching', 'norms'],
    queryFn: () => teachingApi.norms({ page: 0, size: 100 }),
    enabled: isPrivileged,
  });

  const assignments = useQuery({
    queryKey: ['teaching', 'assignments', assignmentYearId],
    queryFn: () => teachingApi.assignments(Number(assignmentYearId)),
    enabled: isPrivileged && !!assignmentYearId,
  });

  const normForm = useForm<{ roleId: string | null; schoolYearId: string | null; requiredHours: number | string; description: string }>({
    initialValues: { roleId: null, schoolYearId: null, requiredHours: 0, description: '' },
    validate: {
      roleId: (value) => (value ? null : 'Izaberite zvanje (rolu)'),
      schoolYearId: (value) => (value ? null : 'Izaberite školsku godinu'),
      requiredHours: (value) => (value === '' || Number(value) < 0 ? 'Unesite broj časova (>= 0)' : null),
    },
  });

  const createNorm = useMutation({
    mutationFn: (values: typeof normForm.values) =>
      teachingApi.createNorm({
        roleId: Number(values.roleId),
        schoolYearId: Number(values.schoolYearId),
        requiredHours: Number(values.requiredHours),
        description: values.description.trim() || null,
      }),
    onSuccess: () => {
      notifySuccess('Norma je sačuvana');
      queryClient.invalidateQueries({ queryKey: ['teaching'] });
      setNormOpen(false);
      normForm.reset();
    },
    onError: (error) => notifyError(error, 'Čuvanje norme nije uspelo'),
  });

  const removeNorm = useMutation({
    mutationFn: (id: number) => teachingApi.removeNorm(id),
    onSuccess: () => {
      notifySuccess('Norma je obrisana');
      queryClient.invalidateQueries({ queryKey: ['teaching'] });
    },
    onError: (error) => notifyError(error, 'Brisanje nije uspelo'),
  });

  const assignForm = useForm<{ workerId: string | null; roleId: string | null; normId: string | null }>({
    initialValues: { workerId: null, roleId: null, normId: null },
    validate: { workerId: (value) => (value ? null : 'Izaberite zaposlenog') },
  });

  const createAssignment = useMutation({
    mutationFn: (values: typeof assignForm.values) =>
      teachingApi.assign({
        schoolYearId: Number(assignmentYearId),
        workerId: Number(values.workerId),
        roleId: values.roleId ? Number(values.roleId) : null,
        normId: values.normId ? Number(values.normId) : null,
      }),
    onSuccess: () => {
      notifySuccess('Zaposleni je dodeljen školskoj godini');
      queryClient.invalidateQueries({ queryKey: ['teaching'] });
      setAssignOpen(false);
      assignForm.reset();
    },
    onError: (error) => notifyError(error, 'Dodela nije uspela'),
  });

  const removeAssignment = useMutation({
    mutationFn: (id: number) => teachingApi.removeAssignment(id),
    onSuccess: () => {
      notifySuccess('Dodela je uklonjena');
      queryClient.invalidateQueries({ queryKey: ['teaching'] });
    },
    onError: (error) => notifyError(error, 'Uklanjanje nije uspelo'),
  });

  const normsForYear = (yearId: string | null) =>
    (norms.data?.content ?? [])
      .filter((norm) => !yearId || norm.schoolYearId === Number(yearId))
      .map((norm) => ({
        value: String(norm.id),
        label: `${roleName(norm.roleId)} — ${norm.requiredHours} h`,
      }));

  return (
    <>
      <PageHeader
        title="Fond časova"
        description="Norma po zvanju, dodele nastavnika i realizovani časovi (UC-HR-03)"
      />

      <QueryState isLoading={schoolYears.isLoading} error={schoolYears.error}>
        <Tabs defaultValue="report">
          <Tabs.List mb="md">
            <Tabs.Tab value="report" leftSection={<IconChartBar size={16} />}>
              Izveštaj
            </Tabs.Tab>
            {isPrivileged && (
              <Tabs.Tab value="norms" leftSection={<IconListNumbers size={16} />}>
                Norme
              </Tabs.Tab>
            )}
            {isPrivileged && (
              <Tabs.Tab value="assignments" leftSection={<IconUsers size={16} />}>
                Dodele nastavnika
              </Tabs.Tab>
            )}
          </Tabs.List>

          <Tabs.Panel value="report">
            <TeachingReportPanel schoolYears={years} />
          </Tabs.Panel>

          {isPrivileged && (
            <Tabs.Panel value="norms">
              <Card withBorder radius="md" p="lg">
                <Group justify="space-between" mb="md">
                  <span>Kvota časova po zvanju za školsku godinu</span>
                  <Button size="xs" leftSection={<IconPlus size={16} />} onClick={() => setNormOpen(true)}>
                    Nova norma
                  </Button>
                </Group>
                <QueryState
                  isLoading={norms.isLoading}
                  error={norms.error}
                  isEmpty={norms.data?.content.length === 0}
                  emptyText="Još uvek nema definisanih normi"
                >
                  <Table highlightOnHover>
                    <Table.Thead>
                      <Table.Tr>
                        <Table.Th>Zvanje (rola)</Table.Th>
                        <Table.Th>Školska godina</Table.Th>
                        <Table.Th>Kvota</Table.Th>
                        <Table.Th>Opis</Table.Th>
                        <Table.Th w={60} />
                      </Table.Tr>
                    </Table.Thead>
                    <Table.Tbody>
                      {norms.data?.content.map((norm) => (
                        <Table.Tr key={norm.id}>
                          <Table.Td>{roleName(norm.roleId)}</Table.Td>
                          <Table.Td>{yearCode(norm.schoolYearId)}</Table.Td>
                          <Table.Td>{formatHours(norm.requiredHours)}</Table.Td>
                          <Table.Td>{dash(norm.description)}</Table.Td>
                          <Table.Td>
                            <Tooltip label="Obriši">
                              <ActionIcon
                                variant="subtle"
                                color="red"
                                onClick={() =>
                                  confirmAction({
                                    title: 'Brisanje norme',
                                    message: 'Da li ste sigurni da želite da obrišete ovu normu?',
                                    confirmLabel: 'Obriši',
                                    onConfirm: () => removeNorm.mutate(norm.id),
                                  })
                                }
                              >
                                <IconTrash size={18} />
                              </ActionIcon>
                            </Tooltip>
                          </Table.Td>
                        </Table.Tr>
                      ))}
                    </Table.Tbody>
                  </Table>
                </QueryState>
              </Card>
            </Tabs.Panel>
          )}

          {isPrivileged && (
            <Tabs.Panel value="assignments">
              <Card withBorder radius="md" p="lg">
                <Group justify="space-between" mb="md" align="flex-end">
                  <Select
                    label="Školska godina"
                    placeholder="Izaberite godinu"
                    w={240}
                    data={yearOptions}
                    value={assignmentYearId}
                    onChange={setAssignmentYearId}
                  />
                  <Button
                    size="xs"
                    leftSection={<IconPlus size={16} />}
                    disabled={!assignmentYearId}
                    onClick={() => setAssignOpen(true)}
                  >
                    Dodeli nastavnika
                  </Button>
                </Group>

                {!assignmentYearId ? (
                  <span>Izaberite školsku godinu da biste videli dodele.</span>
                ) : (
                  <QueryState
                    isLoading={assignments.isLoading}
                    error={assignments.error}
                    isEmpty={assignments.data?.length === 0}
                    emptyText="Za izabranu godinu nema dodeljenih nastavnika"
                  >
                    <Table highlightOnHover>
                      <Table.Thead>
                        <Table.Tr>
                          <Table.Th>Zaposleni</Table.Th>
                          <Table.Th>Zvanje (rola)</Table.Th>
                          <Table.Th>Norma</Table.Th>
                          <Table.Th w={60} />
                        </Table.Tr>
                      </Table.Thead>
                      <Table.Tbody>
                        {assignments.data?.map((assignment) => (
                          <Table.Tr key={assignment.id}>
                            <Table.Td>{workerName(assignment.workerId)}</Table.Td>
                            <Table.Td>{roleName(assignment.roleId)}</Table.Td>
                            <Table.Td>
                              {assignment.normId
                                ? formatHours(
                                    norms.data?.content.find((norm) => norm.id === assignment.normId)?.requiredHours,
                                  )
                                : '—'}
                            </Table.Td>
                            <Table.Td>
                              <Tooltip label="Ukloni">
                                <ActionIcon
                                  variant="subtle"
                                  color="red"
                                  onClick={() =>
                                    confirmAction({
                                      title: 'Uklanjanje dodele',
                                      message: 'Da li ste sigurni da želite da uklonite ovu dodelu?',
                                      confirmLabel: 'Ukloni',
                                      onConfirm: () => removeAssignment.mutate(assignment.id),
                                    })
                                  }
                                >
                                  <IconTrash size={18} />
                                </ActionIcon>
                              </Tooltip>
                            </Table.Td>
                          </Table.Tr>
                        ))}
                      </Table.Tbody>
                    </Table>
                  </QueryState>
                )}
              </Card>
            </Tabs.Panel>
          )}
        </Tabs>
      </QueryState>

      <Modal opened={normOpen} onClose={() => setNormOpen(false)} title="Nova norma">
        <form onSubmit={normForm.onSubmit((values) => createNorm.mutate(values))}>
          <Stack>
            <Select
              label="Zvanje (rola)"
              withAsterisk
              data={(roles.data?.content ?? []).map((role) => ({ value: String(role.id), label: role.name }))}
              {...normForm.getInputProps('roleId')}
            />
            <Select label="Školska godina" withAsterisk data={yearOptions} {...normForm.getInputProps('schoolYearId')} />
            <NumberInput label="Kvota časova" min={0} withAsterisk {...normForm.getInputProps('requiredHours')} />
            <Textarea label="Opis" autosize minRows={2} {...normForm.getInputProps('description')} />
            <Group justify="flex-end">
              <Button variant="default" onClick={() => setNormOpen(false)}>
                Odustani
              </Button>
              <Button type="submit" loading={createNorm.isPending}>
                Sačuvaj
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      <Modal opened={assignOpen} onClose={() => setAssignOpen(false)} title="Dodela nastavnika školskoj godini">
        <form onSubmit={assignForm.onSubmit((values) => createAssignment.mutate(values))}>
          <Stack>
            <Select
              label="Zaposleni"
              withAsterisk
              searchable
              data={(workers.data?.content ?? []).map((worker) => ({
                value: String(worker.id),
                label: worker.fullName,
              }))}
              {...assignForm.getInputProps('workerId')}
            />
            <Select
              label="Zvanje (rola)"
              data={(roles.data?.content ?? []).map((role) => ({ value: String(role.id), label: role.name }))}
              clearable
              {...assignForm.getInputProps('roleId')}
            />
            <Select
              label="Norma"
              description="Kvota koju nastavnik nasleđuje za ovu godinu"
              data={normsForYear(assignmentYearId)}
              clearable
              {...assignForm.getInputProps('normId')}
            />
            <Group justify="flex-end">
              <Button variant="default" onClick={() => setAssignOpen(false)}>
                Odustani
              </Button>
              <Button type="submit" loading={createAssignment.isPending}>
                Sačuvaj
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>
    </>
  );
}
