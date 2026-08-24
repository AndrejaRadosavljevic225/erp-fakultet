import { useState } from 'react';
import {
  ActionIcon,
  Button,
  Card,
  Group,
  Modal,
  Stack,
  Table,
  TextInput,
  Textarea,
  Tooltip,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconPlus, IconTrash } from '@tabler/icons-react';
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { schoolYearsApi } from '../../api/schedule';
import { fieldErrors } from '../../api/client';
import { PageHeader } from '../../components/PageHeader';
import { QueryState } from '../../components/QueryState';
import { SrDateInput } from '../../components/DateFields';
import { TablePagination } from '../../components/TablePagination';
import { dash, formatDate } from '../../lib/format';
import { confirmAction, notifyError, notifySuccess } from '../../lib/notify';

interface FormValues {
  code: string;
  startDate: string | null;
  endDate: string | null;
  description: string;
}

const EMPTY: FormValues = { code: '', startDate: null, endDate: null, description: '' };

export function SchoolYearsPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [formOpen, setFormOpen] = useState(false);
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: ['school-years', 'list', page, size],
    queryFn: () => schoolYearsApi.list({ page, size }),
    placeholderData: keepPreviousData,
  });

  const form = useForm<FormValues>({
    initialValues: EMPTY,
    validate: {
      code: (value) => (value.trim() ? null : 'Oznaka školske godine je obavezna'),
      startDate: (value) => (value ? null : 'Datum početka je obavezan'),
      endDate: (value, values) => {
        if (!value) return 'Datum kraja je obavezan';
        if (values.startDate && value <= values.startDate) return 'Kraj mora biti posle početka';
        return null;
      },
    },
  });

  const create = useMutation({
    mutationFn: (values: FormValues) =>
      schoolYearsApi.create({
        code: values.code.trim(),
        startDate: values.startDate!,
        endDate: values.endDate!,
        description: values.description.trim() || null,
      }),
    onSuccess: () => {
      notifySuccess('Školska godina je kreirana');
      queryClient.invalidateQueries({ queryKey: ['school-years'] });
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
    mutationFn: (id: number) => schoolYearsApi.remove(id),
    onSuccess: () => {
      notifySuccess('Školska godina je obrisana');
      queryClient.invalidateQueries({ queryKey: ['school-years'] });
    },
    onError: (error) => notifyError(error, 'Brisanje nije uspelo'),
  });

  return (
    <>
      <PageHeader
        title="Školske godine"
        description="Period na koji se vezuju norme, dodele i termini"
        action={
          <Button
            leftSection={<IconPlus size={18} />}
            onClick={() => {
              form.setValues(EMPTY);
              setFormOpen(true);
            }}
          >
            Nova školska godina
          </Button>
        }
      />

      <Card withBorder radius="md" p="lg">
        <QueryState
          isLoading={query.isLoading}
          error={query.error}
          isEmpty={query.data?.content.length === 0}
          emptyText="Još uvek nema unetih školskih godina"
        >
          <Table.ScrollContainer minWidth={600}>
            <Table highlightOnHover>
              <Table.Thead>
                <Table.Tr>
                  <Table.Th w={70}>ID</Table.Th>
                  <Table.Th>Oznaka</Table.Th>
                  <Table.Th>Početak</Table.Th>
                  <Table.Th>Kraj</Table.Th>
                  <Table.Th>Opis</Table.Th>
                  <Table.Th w={60} />
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {query.data?.content.map((year) => (
                  <Table.Tr key={year.id}>
                    <Table.Td>{year.id}</Table.Td>
                    <Table.Td fw={500}>{year.code}</Table.Td>
                    <Table.Td>{formatDate(year.startDate)}</Table.Td>
                    <Table.Td>{formatDate(year.endDate)}</Table.Td>
                    <Table.Td>{dash(year.description)}</Table.Td>
                    <Table.Td>
                      <Tooltip label="Obriši">
                        <ActionIcon
                          variant="subtle"
                          color="red"
                          onClick={() =>
                            confirmAction({
                              title: 'Brisanje školske godine',
                              message: `Da li ste sigurni da želite da obrišete „${year.code}”?`,
                              confirmLabel: 'Obriši',
                              onConfirm: () => remove.mutate(year.id),
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

      <Modal opened={formOpen} onClose={() => setFormOpen(false)} title="Nova školska godina">
        <form onSubmit={form.onSubmit((values) => create.mutate(values))}>
          <Stack>
            <TextInput label="Oznaka" placeholder="npr. 2026/2027" withAsterisk {...form.getInputProps('code')} />
            <SrDateInput label="Početak" withAsterisk {...form.getInputProps('startDate')} />
            <SrDateInput label="Kraj" withAsterisk {...form.getInputProps('endDate')} />
            <Textarea label="Opis" autosize minRows={2} {...form.getInputProps('description')} />
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
