import { Button, Group, Modal, Select, SimpleGrid, Stack, TextInput } from '@mantine/core';
import { SrDateInput } from '../../components/DateFields';
import { useForm } from '@mantine/form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { workersApi } from '../../api/hr';
import { fieldErrors } from '../../api/client';
import { employmentStatusLabels, employmentTypeLabels, toSelectData } from '../../lib/labels';
import { notifyError, notifySuccess } from '../../lib/notify';
import type { EmploymentStatus, EmploymentType, Worker } from '../../types/hr';

interface FormValues {
  firstName: string;
  lastName: string;
  email: string;
  personalId: string;
  phone: string;
  /** Mantine date komponente barataju stringom "YYYY-MM-DD" — isti format koji backend ocekuje. */
  hireDate: string | null;
  employmentStatus: EmploymentStatus | null;
  employmentType: EmploymentType | null;
}

const EMPTY: FormValues = {
  firstName: '',
  lastName: '',
  email: '',
  personalId: '',
  phone: '',
  hireDate: null,
  employmentStatus: 'ACTIVE',
  employmentType: 'FULL_TIME',
};

/**
 * Forma za registraciju novog zaposlenog (UC-HR-01) i izmenu postojeceg.
 * Validacija ogleda jakarta anotacije sa backenda; greske sa servera
 * (npr. duplikat JMBG/email) se mapiraju na konkretna polja.
 */
export function WorkerFormModal({
  opened,
  onClose,
  worker,
}: {
  opened: boolean;
  onClose: () => void;
  worker?: Worker | null;
}) {
  const queryClient = useQueryClient();
  const editing = !!worker;

  const form = useForm<FormValues>({
    initialValues: EMPTY,
    validate: {
      firstName: (value) => (value.trim() ? null : 'Ime je obavezno'),
      lastName: (value) => (value.trim() ? null : 'Prezime je obavezno'),
      email: (value) => (/^\S+@\S+\.\S+$/.test(value) ? null : 'Unesite ispravan email'),
      personalId: (value) => (value.trim() ? null : 'JMBG je obavezan'),
      hireDate: (value) => (value ? null : 'Datum zaposlenja je obavezan'),
    },
  });

  const { setValues, resetDirty } = form;

  useEffect(() => {
    if (!opened) return;
    setValues(
      worker
        ? {
            firstName: worker.firstName,
            lastName: worker.lastName,
            email: worker.email,
            personalId: worker.personalId,
            phone: worker.phone ?? '',
            hireDate: worker.hireDate,
            employmentStatus: worker.employmentStatus,
            employmentType: worker.employmentType,
          }
        : EMPTY,
    );
    resetDirty();
  }, [opened, worker, setValues, resetDirty]);

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const body = {
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        email: values.email.trim(),
        personalId: values.personalId.trim(),
        phone: values.phone.trim() || null,
        hireDate: values.hireDate!,
        employmentStatus: values.employmentStatus,
        employmentType: values.employmentType,
      };
      return worker ? workersApi.update(worker.id, body) : workersApi.create(body);
    },
    onSuccess: () => {
      notifySuccess(editing ? 'Podaci su sačuvani' : 'Zaposleni je registrovan');
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
    <Modal
      opened={opened}
      onClose={onClose}
      title={editing ? 'Izmena podataka zaposlenog' : 'Novi zaposleni'}
      size="lg"
    >
      <form onSubmit={form.onSubmit((values) => mutation.mutate(values))}>
        <Stack>
          <SimpleGrid cols={{ base: 1, sm: 2 }}>
            <TextInput label="Ime" withAsterisk {...form.getInputProps('firstName')} />
            <TextInput label="Prezime" withAsterisk {...form.getInputProps('lastName')} />
            <TextInput label="Email" withAsterisk {...form.getInputProps('email')} />
            <TextInput label="JMBG" withAsterisk {...form.getInputProps('personalId')} />
            <TextInput label="Telefon" {...form.getInputProps('phone')} />
            <SrDateInput
              label="Datum zaposlenja"
              withAsterisk
              {...form.getInputProps('hireDate')}
            />
            <Select
              label="Status"
              data={toSelectData(employmentStatusLabels)}
              {...form.getInputProps('employmentStatus')}
            />
            <Select
              label="Vrsta zaposlenja"
              data={toSelectData(employmentTypeLabels)}
              {...form.getInputProps('employmentType')}
            />
          </SimpleGrid>

          <Group justify="flex-end" mt="sm">
            <Button variant="default" onClick={onClose}>
              Odustani
            </Button>
            <Button type="submit" loading={mutation.isPending}>
              {editing ? 'Sačuvaj' : 'Registruj'}
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}
