import { useState } from 'react';
import { Badge, Card, Group, Table, Text, TextInput } from '@mantine/core';
import { IconSearch } from '@tabler/icons-react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { auditApi } from '../../api/hr';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { TablePagination } from '../../components/TablePagination';
import { dash, formatDateTime } from '../../lib/format';

const ACTION_COLORS: Record<string, string> = {
  CREATE: 'green',
  UPDATE: 'blue',
  DELETE: 'red',
  ASSIGN: 'grape',
  REMOVE: 'orange',
};

/** Istorija izmena nad entitetima (UC-G-03). */
export function AuditLogPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [filter, setFilter] = useState('');

  const query = useQuery({
    queryKey: ['audit', 'list', page, size],
    queryFn: () => auditApi.list({ page, size, sort: 'changedAt,desc' }),
    placeholderData: keepPreviousData,
  });

  // Backend nema pretragu nad audit logom, pa se filtrira prikazana strana.
  const rows = (query.data?.content ?? []).filter((log) => {
    if (!filter.trim()) return true;
    const needle = filter.trim().toLowerCase();
    return (
      log.entityName.toLowerCase().includes(needle) ||
      log.action.toLowerCase().includes(needle) ||
      (log.details ?? '').toLowerCase().includes(needle)
    );
  });

  return (
    <>
      <PageHeader title="Istorija izmena" description="Ko je šta i kada izmenio (UC-G-03)" />

      <Card withBorder radius="md" p="lg">
        <Group mb="md">
          <TextInput
            placeholder="Filtriraj prikazanu stranu (entitet, akcija, detalji)"
            leftSection={<IconSearch size={16} />}
            value={filter}
            onChange={(event) => setFilter(event.currentTarget.value)}
            w={420}
          />
        </Group>

        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={rows.length === 0}
          emptyText={filter ? 'Nema rezultata na ovoj strani' : 'Nema evidentiranih izmena'}
        >
          <Table.ScrollContainer minWidth={760}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th>Vreme</Table.Th>
                  <Table.Th>Entitet</Table.Th>
                  <Table.Th w={90}>ID</Table.Th>
                  <Table.Th>Akcija</Table.Th>
                  <Table.Th>Izvršio (korisnik)</Table.Th>
                  <Table.Th>Detalji</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {rows.map((log) => (
                  <Table.Tr key={log.id}>
                    <Table.Td>{formatDateTime(log.changedAt)}</Table.Td>
                    <Table.Td>{log.entityName}</Table.Td>
                    <Table.Td>{log.entityId}</Table.Td>
                    <Table.Td>
                      <Badge color={ACTION_COLORS[log.action] ?? 'gray'} variant="light">
                        {log.action}
                      </Badge>
                    </Table.Td>
                    <Table.Td>{dash(log.changedBy)}</Table.Td>
                    <Table.Td>
                      <Text size="xs" c="dimmed">
                        {dash(log.details)}
                      </Text>
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
    </>
  );
}
