import { Alert, Button, Group, List, Loader, Modal, Select, Stack, TextInput } from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconAlertTriangle, IconCircleCheck } from '@tabler/icons-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { bookingsApi, roomsApi, schoolYearsApi } from '../../api/schedule';
import { workersApi } from '../../api/hr';
import { useAuth } from '../../auth/AuthContext';
import { SrDateTimePicker } from '../../components/DateFields';
import { formatInterval, toApiDateTime } from '../../lib/format';
import { teachingTypeLabels, toSelectData } from '../../lib/labels';
import { notifyError, notifySuccess } from '../../lib/notify';
import type { TeachingType } from '../../types/schedule';

interface FormValues {
  roomId: string | null;
  requesterWorkerId: string | null;
  schoolYearId: string | null;
  /** DateTimePicker vraca "YYYY-MM-DD HH:mm:ss". */
  startDateTime: string | null;
  endDateTime: string | null;
  purpose: string;
  teachingType: TeachingType | null;
}

const EMPTY: FormValues = {
  roomId: null,
  requesterWorkerId: null,
  schoolYearId: null,
  startDateTime: null,
  endDateTime: null,
  purpose: '',
  teachingType: null,
};

/**
 * Kreiranje rezervacije sa proverom preklapanja (UC-SC-02).
 * Dostupnost se proverava cim su izabrani prostorija i oba termina, pre samog slanja.
 */
export function BookingFormModal({ opened, onClose }: { opened: boolean; onClose: () => void }) {
  const queryClient = useQueryClient();
  const { user, isPrivileged } = useAuth();

  const form = useForm<FormValues>({
    initialValues: EMPTY,
    validate: {
      roomId: (value) => (value ? null : 'Izaberite prostoriju'),
      requesterWorkerId: (value) =>
        isPrivileged && !value ? 'Izaberite zaposlenog koji rezerviše' : null,
      startDateTime: (value) => (value ? null : 'Izaberite početak termina'),
      endDateTime: (value, values) => {
        if (!value) return 'Izaberite kraj termina';
        if (values.startDateTime && value <= values.startDateTime) {
          return 'Kraj termina mora biti posle početka';
        }
        return null;
      },
    },
  });

  const { setValues, reset } = form;

  useEffect(() => {
    if (opened) {
      reset();
      setValues({ ...EMPTY, requesterWorkerId: user?.workerId ? String(user.workerId) : null });
    }
  }, [opened, user?.workerId, setValues, reset]);

  const rooms = useQuery({
    queryKey: ['rooms', 'all'],
    queryFn: () => roomsApi.list({ page: 0, size: 200 }),
    enabled: opened,
  });

  const workers = useQuery({
    queryKey: ['workers', 'all'],
    queryFn: () => workersApi.search(undefined, { page: 0, size: 200 }),
    enabled: opened && isPrivileged,
  });

  const schoolYears = useQuery({
    queryKey: ['school-years', 'all'],
    queryFn: () => schoolYearsApi.list({ page: 0, size: 100 }),
    enabled: opened,
  });

  const { roomId, startDateTime, endDateTime } = form.values;
  const canCheck = !!roomId && !!startDateTime && !!endDateTime && endDateTime > startDateTime;

  const availability = useQuery({
    queryKey: ['availability', roomId, startDateTime, endDateTime],
    queryFn: () =>
      bookingsApi.checkAvailability({
        roomId: Number(roomId),
        startDateTime: toApiDateTime(startDateTime)!,
        endDateTime: toApiDateTime(endDateTime)!,
      }),
    enabled: opened && canCheck,
  });

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      bookingsApi.create({
        roomId: Number(values.roomId),
        requesterWorkerId: Number(values.requesterWorkerId ?? user?.workerId),
        schoolYearId: values.schoolYearId ? Number(values.schoolYearId) : null,
        startDateTime: toApiDateTime(values.startDateTime)!,
        endDateTime: toApiDateTime(values.endDateTime)!,
        purpose: values.purpose.trim() || null,
        teachingType: values.teachingType,
      }),
    onSuccess: () => {
      notifySuccess('Rezervacija je poslata i čeka odobrenje');
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
      onClose();
    },
    onError: (error) => notifyError(error, 'Rezervacija nije kreirana'),
  });

  const noWorkerLinked = !isPrivileged && !user?.workerId;

  return (
    <Modal opened={opened} onClose={onClose} title="Nova rezervacija prostorije" size="lg">
      <form onSubmit={form.onSubmit((values) => mutation.mutate(values))}>
        <Stack>
          {noWorkerLinked && (
            <Alert color="red" icon={<IconAlertTriangle size={18} />}>
              Vaš nalog nije povezan sa zaposlenim, pa ne možete podneti rezervaciju. Obratite se administratoru.
            </Alert>
          )}

          <Select
            label="Prostorija"
            withAsterisk
            searchable
            data={(rooms.data?.content ?? [])
              .filter((room) => room.bookable && room.active)
              .map((room) => ({
                value: String(room.id),
                label: `${room.code}${room.name ? ` — ${room.name}` : ''}${
                  room.capacity ? ` (${room.capacity} mesta)` : ''
                }`,
              }))}
            {...form.getInputProps('roomId')}
          />

          {isPrivileged ? (
            <Select
              label="Zaposleni koji rezerviše"
              withAsterisk
              searchable
              data={(workers.data?.content ?? []).map((worker) => ({
                value: String(worker.id),
                label: worker.fullName,
              }))}
              {...form.getInputProps('requesterWorkerId')}
            />
          ) : (
            <TextInput label="Zaposleni koji rezerviše" value={user?.workerFullName ?? ''} disabled />
          )}

          <Group grow align="flex-start">
            <SrDateTimePicker label="Početak termina" withAsterisk {...form.getInputProps('startDateTime')} />
            <SrDateTimePicker label="Kraj termina" withAsterisk {...form.getInputProps('endDateTime')} />
          </Group>

          {canCheck && (
            <>
              {availability.isFetching && (
                <Group gap="xs">
                  <Loader size="xs" />
                  <span>Provera dostupnosti…</span>
                </Group>
              )}
              {!availability.isFetching && availability.data?.available && (
                <Alert color="green" icon={<IconCircleCheck size={18} />}>
                  Termin je slobodan.
                </Alert>
              )}
              {!availability.isFetching && availability.data && !availability.data.available && (
                <Alert color="red" icon={<IconAlertTriangle size={18} />} title="Termin je zauzet">
                  <List size="sm">
                    {availability.data.conflicts.map((conflict) => (
                      <List.Item key={conflict.id}>
                        {formatInterval(conflict.startDateTime, conflict.endDateTime)}
                        {conflict.purpose ? ` — ${conflict.purpose}` : ''}
                      </List.Item>
                    ))}
                  </List>
                </Alert>
              )}
            </>
          )}

          <Group grow align="flex-start">
            <Select
              label="Školska godina"
              placeholder="Opciono"
              clearable
              data={(schoolYears.data?.content ?? []).map((year) => ({
                value: String(year.id),
                label: year.code,
              }))}
              {...form.getInputProps('schoolYearId')}
            />
            <Select
              label="Tip nastave"
              placeholder="Nenastavna rezervacija"
              description="Samo nastavni termini ulaze u fond časova"
              clearable
              data={toSelectData(teachingTypeLabels)}
              {...form.getInputProps('teachingType')}
            />
          </Group>

          <TextInput label="Svrha" placeholder="npr. Predavanje iz Baza podataka" {...form.getInputProps('purpose')} />

          <Group justify="flex-end" mt="sm">
            <Button variant="default" onClick={onClose}>
              Odustani
            </Button>
            <Button
              type="submit"
              loading={mutation.isPending}
              disabled={noWorkerLinked || availability.data?.available === false}
            >
              Rezerviši
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
}
