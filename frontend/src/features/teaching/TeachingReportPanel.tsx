import { Alert, Badge, Card, Group, Progress, SimpleGrid, Select, Stack, Table, Text, Title } from '@mantine/core';
import { IconInfoCircle } from '@tabler/icons-react';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { teachingApi } from '../../api/schedule';
import { workersApi } from '../../api/hr';
import { useAuth } from '../../auth/AuthContext';
import { QueryState } from '../../components/QueryState';
import { formatHours } from '../../lib/format';
import type { SchoolYear } from '../../types/schedule';

function StatTile({ label, value, color }: { label: string; value: string; color?: string }) {
  return (
    <Card withBorder radius="md" p="md">
      <Text size="xs" c="dimmed" tt="uppercase" fw={700}>
        {label}
      </Text>
      <Text size="xl" fw={700} c={color}>
        {value}
      </Text>
    </Card>
  );
}

/**
 * Fond casova: norma (kvota po zvanju za skolsku godinu) vs realizovani sati
 * iz odrzanih nastavnih termina, sa odstupanjem i prekovremenim satima (UC-HR-03).
 */
export function TeachingReportPanel({ schoolYears }: { schoolYears: SchoolYear[] }) {
  const { user, isPrivileged } = useAuth();
  const [schoolYearId, setSchoolYearId] = useState<string | null>(
    schoolYears.length ? String(schoolYears[0].id) : null,
  );
  const [workerId, setWorkerId] = useState<string | null>(user?.workerId ? String(user.workerId) : null);

  const workers = useQuery({
    queryKey: ['workers', 'all'],
    queryFn: () => workersApi.search(undefined, { page: 0, size: 200 }),
    enabled: isPrivileged,
  });

  const report = useQuery({
    queryKey: ['teaching', 'report', workerId, schoolYearId],
    queryFn: () => teachingApi.report(Number(workerId), Number(schoolYearId)),
    enabled: !!workerId && !!schoolYearId,
  });

  const yearReport = useQuery({
    queryKey: ['teaching', 'report-year', schoolYearId],
    queryFn: () => teachingApi.reportByYear(Number(schoolYearId)),
    enabled: isPrivileged && !!schoolYearId,
  });

  const data = report.data;
  const required = data?.requiredHours ?? 0;
  const percent = required > 0 ? Math.min(100, Math.round((data!.realizedHours / required) * 100)) : 0;

  const workerName = (id: number) =>
    workers.data?.content.find((worker) => worker.id === id)?.fullName ?? `zaposleni #${id}`;

  return (
    <Stack>
      <Group align="flex-end">
        <Select
          label="Školska godina"
          w={220}
          allowDeselect={false}
          data={schoolYears.map((year) => ({ value: String(year.id), label: year.code }))}
          value={schoolYearId}
          onChange={setSchoolYearId}
        />
        {isPrivileged ? (
          <Select
            label="Zaposleni"
            w={260}
            searchable
            data={(workers.data?.content ?? []).map((worker) => ({
              value: String(worker.id),
              label: worker.fullName,
            }))}
            value={workerId}
            onChange={setWorkerId}
          />
        ) : (
          <Text size="sm" c="dimmed" pb={8}>
            Prikazan je vaš fond časova.
          </Text>
        )}
      </Group>

      {!workerId && (
        <Alert color="blue" icon={<IconInfoCircle size={18} />}>
          {isPrivileged
            ? 'Izaberite zaposlenog da biste videli njegov fond časova.'
            : 'Vaš nalog nije povezan sa zaposlenim, pa fond časova ne može da se prikaže.'}
        </Alert>
      )}

      {workerId && schoolYearId && (
        <QueryState isLoading={report.isLoading} error={report.error}>
          {data && (
            <>
              <SimpleGrid cols={{ base: 1, sm: 2, lg: 4 }}>
                <StatTile label="Norma (kvota)" value={formatHours(data.requiredHours)} />
                <StatTile label="Realizovano" value={formatHours(data.realizedHours)} />
                <StatTile
                  label="Odstupanje"
                  value={formatHours(data.deviation)}
                  color={data.deviation < 0 ? 'red' : 'green'}
                />
                <StatTile label="Prekovremeni" value={formatHours(data.extraHours)} color="orange" />
              </SimpleGrid>

              <Card withBorder radius="md" p="lg">
                <Group justify="space-between" mb="xs">
                  <Text size="sm" fw={500}>
                    Ispunjenost norme
                  </Text>
                  <Badge color={data.fulfilled ? 'green' : 'yellow'} variant="light">
                    {data.fulfilled ? 'Norma ispunjena' : `${percent}% norme`}
                  </Badge>
                </Group>
                <Progress value={percent} color={data.fulfilled ? 'green' : 'blue'} size="lg" radius="sm" />
                {data.requiredHours == null && (
                  <Text size="xs" c="dimmed" mt="xs">
                    Za ovog zaposlenog nije definisana norma za izabranu školsku godinu.
                  </Text>
                )}
              </Card>
            </>
          )}
        </QueryState>
      )}

      {isPrivileged && schoolYearId && (
        <Card withBorder radius="md" p="lg">
          <Title order={4} mb="md">
            Pregled po svim zaposlenima
          </Title>
          <QueryState
            isLoading={yearReport.isLoading}
            error={yearReport.error}
            isEmpty={yearReport.data?.length === 0}
            emptyText="Za izabranu godinu nema dodeljenih nastavnika"
          >
            <Table.ScrollContainer minWidth={600}>
              <Table highlightOnHover>
                <Table.Thead>
                  <Table.Tr>
                    <Table.Th>Zaposleni</Table.Th>
                    <Table.Th>Norma</Table.Th>
                    <Table.Th>Realizovano</Table.Th>
                    <Table.Th>Odstupanje</Table.Th>
                    <Table.Th>Prekovremeni</Table.Th>
                    <Table.Th>Status</Table.Th>
                  </Table.Tr>
                </Table.Thead>
                <Table.Tbody>
                  {yearReport.data?.map((row) => (
                    <Table.Tr key={row.workerId}>
                      <Table.Td>{workerName(row.workerId)}</Table.Td>
                      <Table.Td>{formatHours(row.requiredHours)}</Table.Td>
                      <Table.Td>{formatHours(row.realizedHours)}</Table.Td>
                      <Table.Td c={row.deviation < 0 ? 'red' : 'green'}>{formatHours(row.deviation)}</Table.Td>
                      <Table.Td>{formatHours(row.extraHours)}</Table.Td>
                      <Table.Td>
                        <Badge color={row.fulfilled ? 'green' : 'yellow'} variant="light">
                          {row.fulfilled ? 'Ispunjena' : 'Nije ispunjena'}
                        </Badge>
                      </Table.Td>
                    </Table.Tr>
                  ))}
                </Table.Tbody>
              </Table>
            </Table.ScrollContainer>
          </QueryState>
        </Card>
      )}
    </Stack>
  );
}
