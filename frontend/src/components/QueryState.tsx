import { Alert, Center, Loader, Stack, Text } from '@mantine/core';
import { IconAlertCircle, IconInbox } from '@tabler/icons-react';
import type { ReactNode } from 'react';
import { errorMessage } from '../api/client';

/**
 * Jedinstveni prikaz stanja upita: ucitavanje / greska / prazna lista / sadrzaj.
 * Drzi sve tabele u aplikaciji doslednim.
 */
export function QueryState({
  isLoading,
  error,
  isEmpty,
  emptyText = 'Nema podataka za prikaz',
  children,
}: {
  isLoading: boolean;
  error?: unknown;
  isEmpty?: boolean;
  emptyText?: string;
  children: ReactNode;
}) {
  if (isLoading) {
    return (
      <Center py="xl">
        <Loader />
      </Center>
    );
  }

  if (error) {
    return (
      <Alert color="red" icon={<IconAlertCircle size={18} />} title="Greška pri učitavanju">
        {errorMessage(error)}
      </Alert>
    );
  }

  if (isEmpty) {
    return (
      <Center py="xl">
        <Stack align="center" gap={4}>
          <IconInbox size={32} opacity={0.4} />
          <Text c="dimmed" size="sm">
            {emptyText}
          </Text>
        </Stack>
      </Center>
    );
  }

  return <>{children}</>;
}
