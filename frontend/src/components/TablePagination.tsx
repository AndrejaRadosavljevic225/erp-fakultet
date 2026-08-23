import { Group, Pagination, Select, Text } from '@mantine/core';
import type { PageResponse } from '../types/api';

/**
 * Podnozje tabele: broj rezultata, izbor velicine strane i paginacija.
 * `page` je 0-baziran (kao Spring), Mantine Pagination je 1-baziran.
 */
export function TablePagination<T>({
  data,
  page,
  size,
  onPageChange,
  onSizeChange,
}: {
  data: PageResponse<T> | undefined;
  page: number;
  size: number;
  onPageChange: (page: number) => void;
  onSizeChange: (size: number) => void;
}) {
  if (!data || data.totalElements === 0) return null;

  const from = data.pageNumber * data.pageSize + 1;
  const to = Math.min(from + data.content.length - 1, data.totalElements);

  return (
    <Group justify="space-between" mt="md" wrap="wrap">
      <Text size="sm" c="dimmed">
        Prikazano {from}–{to} od {data.totalElements}
      </Text>
      <Group gap="sm">
        <Select
          size="xs"
          w={110}
          data={['10', '20', '50', '100']}
          value={String(size)}
          onChange={(value) => value && onSizeChange(Number(value))}
          allowDeselect={false}
          aria-label="Rezultata po strani"
        />
        <Pagination
          size="sm"
          total={data.totalPages}
          value={page + 1}
          onChange={(value) => onPageChange(value - 1)}
        />
      </Group>
    </Group>
  );
}
