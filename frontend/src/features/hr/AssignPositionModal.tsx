import { Button, Group, Modal, NumberInput, Select, Stack, Switch } from '@mantine/core';
import { SrDateInput } from '../../components/DateFields';
import { useForm } from '@mantine/form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { positionsApi, workerPositionsApi } from '../../api/hr';
import { fieldErrors } from '../../api/client';
import { notifyError, notifySuccess } from '../../lib/notify';
import type { WorkerPosition } from '../../types/hr';

interface FormValues {
  positionId: string | null;
  validFrom: string | null;
  validTo: string | null;
  fraction: number | string;
  isPrimary: boolean;
}

const EMPTY: FormValues = {
  positionId: null,
  validFrom: null,
  validTo: null,
  fraction: 1,
  isPrimary: false,
};

/** Dodela radnog mesta zaposlenom i izmena postojece dodele (UC-HR-05). */
export function AssignPositionModal({
  opened,
  onClose,
  workerId,
  assignment,
}: {
  opened: boolean;
  onClose: () => void;
  workerId: number;
  assignment?: WorkerPosition | null;
}) {
  const queryClient = useQueryClient();
  const editing = !!assignment;

  const positions = useQuery({
    queryKey: ['positions', 'all'],
    queryFn: () => positionsApi.list({ page: 0, size: 100 }),
    enabled: opened,
  });

  const form = useForm<FormValues>({
    initialValues: EMPTY,
    validate: {
      positionId: (value) => (value ? null : 'Izaberite radno mesto'),
      validFrom: (value) => (value ? null : 'Datum početka je obavezan'),
    },
  });

  const { setValues } = form;

  useEffect(() => {
    if (!opened) return;
    const next: FormValues = assignment
      ? {
          positionId: String(assignment.positionId),
          validFrom: assignment.validFrom,
          validTo: assignment.validTo,
          fraction: assignment.fraction ?? 1,
          isPrimary: assignment.isPrimary ?? false,
        }
      : EMPTY;
    setValues(next);
  }, [opened, assignment, setValues]);

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const common = {
        validFrom: values.validFrom!,
        validTo: values.validTo,
        fraction: values.fraction === '' ? null : Number(values.fraction),
        isPrimary: values.isPrimary,
      };
      return assignment
        ? workerPositionsApi.update(assignment.id, common)
        : workerPositionsApi.assign({ ...common, workerId, positionId: Number(values.positionId) });
    },
    onSuccess: () => {
      notifySuccess(editing ? 'Dodela je izmenjena' : 'Radno mesto je dodeljeno');
      queryClient.invalidateQueries({ queryKey: ['workers'] });
      onClose();
    },
    onError: (error) => {
      const errors = fieldErrors(error);
      if (Object.keys(errors).length) {
        form.setErrors(errors);
      } else {
        notifyError(error, 'Čuvanje nije uspelo');
      }
    },
  });

  return (
    <Modal opened={opened} onClose={onClose} title={editing ? 'Izmena dodele' : 'Dodela radnog mesta'}>
      <form onSubmit={form.onSubmit((values) => mutation.mutate(values))}>
        <Stack>
          <Select
            label="Radno mesto"
            withAsterisk
            disabled={editing}
            searchable
            data={(positions.data?.content ?? []).map((position) => ({
              value: String(position.id),
              label: position.title,
            }))}
            {...form.getInputProps('positionId')}
          />
          <SrDateInput
            label="Važi od"
            withAsterisk
            {...form.getInputProps('validFrom')}
          />
          <SrDateInput label="Važi do" {...form.getInputProps('validTo')} />
          <NumberInput
            label="Angažovanje (deo radnog vremena)"
            description="1 = puno radno vreme, 0.5 = pola"
            min={0}
            max={1}
            step={0.1}
            decimalScale={2}
            {...form.getInputProps('fraction')}
          />
          <Switch
            label="Primarno radno mesto"
            {...form.getInputProps('isPrimary', { type: 'checkbox' })}
          />

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
