import { useState } from 'react';
import {
  ActionIcon,
  Anchor,
  Badge,
  Button,
  Card,
  Grid,
  Group,
  Stack,
  Table,
  Text,
  Title,
  Tooltip,
} from '@mantine/core';
import {
  IconArrowLeft,
  IconEdit,
  IconPencil,
  IconPlus,
  IconRefreshAlert,
  IconTrash,
} from '@tabler/icons-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { auditApi, workerPositionsApi, workersApi } from '../../api/hr';
import { useAuth } from '../../auth/AuthContext';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { dash, formatDate, formatDateTime } from '../../lib/format';
import { employmentStatusColors, employmentStatusLabels, employmentTypeLabels } from '../../lib/labels';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';
import type { WorkerPosition } from '../../types/hr';
import { AssignPositionModal } from './AssignPositionModal';
import { StatusChangeModal } from './StatusChangeModal';
import { WorkerFormModal } from './WorkerFormModal';

function InfoRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <Group justify="space-between" wrap="nowrap" gap="xl">
      <Text size="sm" c="dimmed">
        {label}
      </Text>
      {/* component="div" jer vrednost moze biti Badge (div) — <p> ne sme da sadrzi blok element */}
      <Text component="div" size="sm" fw={500} ta="right">
        {value}
      </Text>
    </Group>
  );
}

export function WorkerDetailPage() {
  const { id } = useParams();
  const workerId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { isPrivileged } = useAuth();

  const [editOpen, setEditOpen] = useState(false);
  const [statusOpen, setStatusOpen] = useState(false);
  const [assignOpen, setAssignOpen] = useState(false);
  const [editingAssignment, setEditingAssignment] = useState<WorkerPosition | null>(null);

  const worker = useQuery({
    queryKey: ['workers', 'detail', workerId],
    queryFn: () => workersApi.getById(workerId),
    enabled: Number.isFinite(workerId),
  });

  const audit = useQuery({
    queryKey: ['audit', 'Worker', workerId],
    queryFn: () => auditApi.forEntity('Worker', workerId),
    enabled: isPrivileged && Number.isFinite(workerId),
  });

  const removeAssignment = useMutation({
    mutationFn: (assignmentId: number) => workerPositionsApi.remove(assignmentId),
    onSuccess: () => {
      notifySuccess('Dodela je uklonjena');
      queryClient.invalidateQueries({ queryKey: ['workers'] });
    },
    onError: (error) => notifyError(error, 'Uklanjanje nije uspelo'),
  });

  const data = worker.data;

  return (
    <>
      <Anchor component="button" type="button" onClick={() => navigate('/workers')} mb="sm">
        <Group gap={4}>
          <IconArrowLeft size={16} />
          <Text size="sm">Nazad na listu</Text>
        </Group>
      </Anchor>

      <QueryState isLoading={worker.isLoading} error={worker.error}>
        {data && (
          <>
            <PageHeader
              title={data.fullName}
              description={`Profil zaposlenog #${data.id}`}
              action={
                isPrivileged && (
                  <Group>
                    <Button
                      variant="default"
                      leftSection={<IconRefreshAlert size={18} />}
                      onClick={() => setStatusOpen(true)}
                    >
                      Promena statusa
                    </Button>
                    <Button leftSection={<IconEdit size={18} />} onClick={() => setEditOpen(true)}>
                      Izmeni
                    </Button>
                  </Group>
                )
              }
            />

            <Grid>
              <Grid.Col span={{ base: 12, md: 5 }}>
                <Card withBorder radius="md" p="lg">
                  <Title order={4} mb="md">
                    Lični podaci
                  </Title>
                  <Stack gap="xs">
                    <InfoRow
                      label="Status"
                      value={
                        <Badge color={employmentStatusColors[data.employmentStatus]} variant="light">
                          {employmentStatusLabels[data.employmentStatus]}
                        </Badge>
                      }
                    />
                    <InfoRow label="Email" value={data.email} />
                    <InfoRow label="JMBG" value={data.personalId} />
                    <InfoRow label="Telefon" value={dash(data.phone)} />
                    <InfoRow label="Datum zaposlenja" value={formatDate(data.hireDate)} />
                    <InfoRow label="Prestanak radnog odnosa" value={formatDate(data.terminationDate)} />
                    <InfoRow
                      label="Vrsta zaposlenja"
                      value={data.employmentType ? employmentTypeLabels[data.employmentType] : '—'}
                    />
                  </Stack>
                </Card>
              </Grid.Col>

              <Grid.Col span={{ base: 12, md: 7 }}>
                <Card withBorder radius="md" p="lg">
                  <Group justify="space-between" mb="md">
                    <Title order={4}>Radna mesta</Title>
                    {isPrivileged && (
                      <Button
                        size="xs"
                        leftSection={<IconPlus size={16} />}
                        onClick={() => {
                          setEditingAssignment(null);
                          setAssignOpen(true);
                        }}
                      >
                        Dodeli
                      </Button>
                    )}
                  </Group>

                  <QueryState
                    isLoading={false}
                    isEmpty={data.positions.length === 0}
                    emptyText="Zaposlenom nije dodeljeno nijedno radno mesto"
                  >
                    <Table.ScrollContainer minWidth={480}>
                      <Table>
                        <Table.Thead>
                          <Table.Tr>
                            <Table.Th>Radno mesto</Table.Th>
                            <Table.Th>Period</Table.Th>
                            <Table.Th>Angažovanje</Table.Th>
                            <Table.Th w={90} />
                          </Table.Tr>
                        </Table.Thead>
                        <Table.Tbody>
                          {data.positions.map((assignment) => (
                            <Table.Tr key={assignment.id}>
                              <Table.Td>
                                <Group gap={6}>
                                  {assignment.positionTitle ?? `#${assignment.positionId}`}
                                  {assignment.isPrimary && (
                                    <Badge size="xs" variant="light">
                                      primarno
                                    </Badge>
                                  )}
                                </Group>
                              </Table.Td>
                              <Table.Td>
                                {formatDate(assignment.validFrom)} – {formatDate(assignment.validTo)}
                              </Table.Td>
                              <Table.Td>{assignment.fraction ?? '—'}</Table.Td>
                              <Table.Td>
                                {isPrivileged && (
                                  <Group gap={4} justify="flex-end" wrap="nowrap">
                                    <Tooltip label="Izmeni">
                                      <ActionIcon
                                        variant="subtle"
                                        onClick={() => {
                                          setEditingAssignment(assignment);
                                          setAssignOpen(true);
                                        }}
                                      >
                                        <IconPencil size={16} />
                                      </ActionIcon>
                                    </Tooltip>
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
                                        <IconTrash size={16} />
                                      </ActionIcon>
                                    </Tooltip>
                                  </Group>
                                )}
                              </Table.Td>
                            </Table.Tr>
                          ))}
                        </Table.Tbody>
                      </Table>
                    </Table.ScrollContainer>
                  </QueryState>
                </Card>
              </Grid.Col>
            </Grid>

            {isPrivileged && (
              <Card withBorder radius="md" p="lg" mt="md">
                <Title order={4} mb="md">
                  Istorija izmena
                </Title>
                <QueryState
                  isLoading={audit.isLoading}
                  error={audit.error}
                  isEmpty={audit.data?.length === 0}
                  emptyText="Nema evidentiranih izmena"
                >
                  <Table.ScrollContainer minWidth={520}>
                    <Table>
                      <Table.Thead>
                        <Table.Tr>
                          <Table.Th>Vreme</Table.Th>
                          <Table.Th>Akcija</Table.Th>
                          <Table.Th>Izvršio (korisnik)</Table.Th>
                          <Table.Th>Detalji</Table.Th>
                        </Table.Tr>
                      </Table.Thead>
                      <Table.Tbody>
                        {audit.data?.map((log) => (
                          <Table.Tr key={log.id}>
                            <Table.Td>{formatDateTime(log.changedAt)}</Table.Td>
                            <Table.Td>
                              <Badge variant="light" size="sm">
                                {log.action}
                              </Badge>
                            </Table.Td>
                            <Table.Td>{dash(log.changedBy)}</Table.Td>
                            <Table.Td>
                              <Text size="xs" c="dimmed" lineClamp={2}>
                                {dash(log.details)}
                              </Text>
                            </Table.Td>
                          </Table.Tr>
                        ))}
                      </Table.Tbody>
                    </Table>
                  </Table.ScrollContainer>
                </QueryState>
              </Card>
            )}

            <WorkerFormModal opened={editOpen} onClose={() => setEditOpen(false)} worker={data} />
            <StatusChangeModal opened={statusOpen} onClose={() => setStatusOpen(false)} worker={data} />
            <AssignPositionModal
              opened={assignOpen}
              onClose={() => setAssignOpen(false)}
              workerId={data.id}
              assignment={editingAssignment}
            />
          </>
        )}
      </QueryState>
    </>
  );
}
