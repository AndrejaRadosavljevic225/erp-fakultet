import { Alert, Button, Group, Modal, Select, Stack, Text } from '@mantine/core';
import { SrDateInput } from '../../components/DateFields';
import { useForm } from '@mantine/form';
import { IconInfoCircle } from '@tabler/icons-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { workersApi } from '../../api/hr';
import { employmentStatusLabels, toSelectData } from '../../lib/labels';
import { notifyError, notifySuccess } from '../../lib/notify';
import type { EmploymentStatus, WorkerDetail } from '../../types/hr';

/**
 * Promena statusa zaposlenog: odsustvo, suspenzija, prestanak radnog odnosa (UC-HR-06).
 * Backend svaku izmenu radnika belezi u audit_log, pa je istorija promena vidljiva na profilu.
 */
export function StatusChangeModal({
  opened,
  onClose,
  worker,
}: {
  opened: boolean;
  onClose: () => void;
  worker: WorkerDetail;
}) {
  const queryClient = useQueryClient();

  const form = useForm<{ employmentStatus: EmploymentStatus; terminationDate: string | null }>({
    initialValues: {
      employmentStatus: worker.employmentStatus,
      terminationDate: worker.terminationDate,
    },
  });

  const { setValues } = form;

  useEffect(() => {
    if (opened) {
      setValues({ employmentStatus: worker.employmentStatus, terminationDate: worker.terminationDate });
    }
  }, [opened, worker, setValues]);

  const mutation = useMutation({
    mutationFn: (values: { employmentStatus: EmploymentStatus; terminationDate: string | null }) =>
      workersApi.update(worker.id, {
        employmentStatus: values.employmentStatus,
        terminationDate: values.employmentStatus === 'TERMINATED' ? values.terminationDate : null,
      }),
    onSuccess: () => {
      notifySuccess('Status zaposlenog je promenjen');
      queryClient.invalidateQueries({ queryKey: ['workers'] });
      queryClient.invalidateQueries({ queryKey: ['audit'] });
      onClose();
    },
    onError: (error) => notifyError(error, 'Promena statusa nije uspela'),
  });

  const terminated = form.values.employmentStatus === 'TERMINATED';

  return (
    <Modal opened={opened} onClose={onClose} title="Promena statusa zaposlenog">
      <form onSubmit={form.onSubmit((values) => mutation.mutate(values))}>
        <Stack>
          <Text size="sm" c="dimmed">
            {worker.fullName}
          </Text>

          <Select
            label="Novi status"
            data={toSelectData(employmentStatusLabels)}
            allowDeselect={false}
            {...form.getInputProps('employmentStatus')}
          />

          {terminated && (
            <SrDateInput
              label="Datum prestanka radnog odnosa"
              {...form.getInputProps('terminationDate')}
            />
          )}

          <Alert color="blue" icon={<IconInfoCircle size={18} />}>
            Promena se evidentira u istoriji izmena (audit log) sa korisnikom koji ju je izvršio.
          </Alert>

          <Group justify="flex-end">
            <Button variant="default" onClick={onClose}>
              Odustani
            </Button>
            <Button type="submit" loading={mutation.isPending}>
              Sačuvaj
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}
