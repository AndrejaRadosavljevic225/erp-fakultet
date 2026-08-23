import { Button, Group, Modal, NumberInput, Stack, Switch, TextInput } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { positionsApi } from '../../api/hr';
import { fieldErrors } from '../../api/client';
import { notifyError, notifySuccess } from '../../lib/notify';
import type { Position } from '../../types/hr';

interface FormValues {
  title: string;
  salaryGrade: string;
  baseSalary: number | string;
  isVacant: boolean;
}

const EMPTY: FormValues = { title: '', salaryGrade: '', baseSalary: '', isVacant: false };

/** Kreiranje i izmena radnog mesta (UC-HR-05). */
export function PositionFormModal({
  opened,
  onClose,
  position,
}: {
  opened: boolean;
  onClose: () => void;
  position?: Position | null;
}) {
  const queryClient = useQueryClient();
  const editing = !!position;

  const form = useForm<FormValues>({
    initialValues: EMPTY,
    validate: { title: (value) => (value.trim() ? null : 'Naziv radnog mesta je obavezan') },
  });

  const { setValues } = form;

  useEffect(() => {
    if (!opened) return;
    setValues(
      position
        ? {
            title: position.title,
            salaryGrade: position.salaryGrade ?? '',
            baseSalary: position.baseSalary ?? '',
            isVacant: position.vacant,
          }
        : EMPTY,
    );
  }, [opened, position, setValues]);

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const body = {
        title: values.title.trim(),
        salaryGrade: values.salaryGrade.trim() || null,
        baseSalary: values.baseSalary === '' ? null : Number(values.baseSalary),
        isVacant: values.isVacant,
      };
      return position ? positionsApi.update(position.id, body) : positionsApi.create(body);
    },
    onSuccess: () => {
      notifySuccess(editing ? 'Radno mesto je izmenjeno' : 'Radno mesto je kreirano');
      queryClient.invalidateQueries({ queryKey: ['positions'] });
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
    <Modal opened={opened} onClose={onClose} title={editing ? 'Izmena radnog mesta' : 'Novo radno mesto'}>
      <form onSubmit={form.onSubmit((values) => mutation.mutate(values))}>
        <Stack>
          <TextInput label="Naziv" withAsterisk {...form.getInputProps('title')} />
          <TextInput label="Platni razred" {...form.getInputProps('salaryGrade')} />
          <NumberInput
            label="Osnovna plata (RSD)"
            min={0}
            thousandSeparator="."
            decimalSeparator=","
            {...form.getInputProps('baseSalary')}
          />
          <Switch
            label="Radno mesto je slobodno (upražnjeno)"
            {...form.getInputProps('isVacant', { type: 'checkbox' })}
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
