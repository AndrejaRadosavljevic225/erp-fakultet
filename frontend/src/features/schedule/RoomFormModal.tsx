import { Button, Group, Modal, NumberInput, SimpleGrid, Stack, Switch, TextInput } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { roomsApi } from '../../api/schedule';
import { fieldErrors } from '../../api/client';
import { notifyError, notifySuccess } from '../../lib/notify';
import type { Room } from '../../types/schedule';

interface FormValues {
  code: string;
  name: string;
  building: string;
  floor: number | string;
  roomNumber: string;
  capacity: number | string;
  roomType: string;
  computerCount: number | string;
  bookable: boolean;
  active: boolean;
}

const EMPTY: FormValues = {
  code: '',
  name: '',
  building: '',
  floor: '',
  roomNumber: '',
  capacity: '',
  roomType: '',
  computerCount: '',
  bookable: true,
  active: true,
};

/** Kreiranje i izmena prostorije (UC-SC-01). */
export function RoomFormModal({
  opened,
  onClose,
  room,
}: {
  opened: boolean;
  onClose: () => void;
  room?: Room | null;
}) {
  const queryClient = useQueryClient();
  const editing = !!room;

  const form = useForm<FormValues>({
    initialValues: EMPTY,
    validate: { code: (value) => (value.trim() ? null : 'Oznaka prostorije je obavezna') },
  });

  const { setValues } = form;

  useEffect(() => {
    if (!opened) return;
    const next: FormValues = room
      ? {
          code: room.code,
          name: room.name ?? '',
          building: room.building ?? '',
          floor: room.floor ?? '',
          roomNumber: room.roomNumber ?? '',
          capacity: room.capacity ?? '',
          roomType: room.roomType ?? '',
          computerCount: room.computerCount ?? '',
          bookable: room.bookable,
          active: room.active,
        }
      : EMPTY;
    setValues(next);
  }, [opened, room, setValues]);

  const num = (value: number | string) => (value === '' ? null : Number(value));

  const mutation = useMutation({
    mutationFn: (values: FormValues) => {
      const body = {
        name: values.name.trim() || null,
        building: values.building.trim() || null,
        floor: num(values.floor),
        roomNumber: values.roomNumber.trim() || null,
        capacity: num(values.capacity),
        roomType: values.roomType.trim() || null,
        computerCount: num(values.computerCount),
        bookable: values.bookable,
      };
      return room
        ? roomsApi.update(room.id, { ...body, active: values.active })
        : roomsApi.create({ ...body, code: values.code.trim() });
    },
    onSuccess: () => {
      notifySuccess(editing ? 'Prostorija je izmenjena' : 'Prostorija je kreirana');
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
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
    <Modal opened={opened} onClose={onClose} title={editing ? 'Izmena prostorije' : 'Nova prostorija'} size="lg">
      <form onSubmit={form.onSubmit((values) => mutation.mutate(values))}>
        <Stack>
          <SimpleGrid cols={{ base: 1, sm: 2 }}>
            <TextInput
              label="Oznaka"
              placeholder="npr. RAF-101"
              withAsterisk
              disabled={editing}
              {...form.getInputProps('code')}
            />
            <TextInput label="Naziv" placeholder="npr. Amfiteatar 1" {...form.getInputProps('name')} />
            <TextInput label="Zgrada" {...form.getInputProps('building')} />
            <TextInput label="Broj prostorije" {...form.getInputProps('roomNumber')} />
            <NumberInput label="Sprat" {...form.getInputProps('floor')} />
            <NumberInput label="Kapacitet" min={0} {...form.getInputProps('capacity')} />
            <TextInput label="Tip" placeholder="npr. AMPHITHEATER, LAB" {...form.getInputProps('roomType')} />
            <NumberInput label="Broj računara" min={0} {...form.getInputProps('computerCount')} />
          </SimpleGrid>

          <Group>
            <Switch label="Može se rezervisati" {...form.getInputProps('bookable', { type: 'checkbox' })} />
            {editing && <Switch label="Aktivna" {...form.getInputProps('active', { type: 'checkbox' })} />}
          </Group>

          <Group justify="flex-end" mt="sm">
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
