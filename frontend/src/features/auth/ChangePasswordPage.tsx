import { Button, Card, PasswordInput, Stack } from '@mantine/core';
import { useForm } from '@mantine/form';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../../api/hr';
import { PageHeader } from '../../components/PageHeader';
import { notifyError, notifySuccess } from '../../lib/notify';

export function ChangePasswordPage() {
  const navigate = useNavigate();

  const form = useForm({
    initialValues: { oldPassword: '', newPassword: '', confirmPassword: '' },
    validate: {
      oldPassword: (value) => (value ? null : 'Unesite staru lozinku'),
      newPassword: (value) => (value.length >= 6 ? null : 'Nova lozinka mora imati bar 6 karaktera'),
      confirmPassword: (value, values) => (value === values.newPassword ? null : 'Lozinke se ne poklapaju'),
    },
  });

  const mutation = useMutation({
    mutationFn: (values: { oldPassword: string; newPassword: string }) =>
      authApi.changePassword(values.oldPassword, values.newPassword),
    onSuccess: () => {
      notifySuccess('Lozinka je promenjena');
      navigate('/');
    },
    onError: (error) => notifyError(error, 'Promena lozinke nije uspela'),
  });

  return (
    <>
      <PageHeader title="Promena lozinke" description="Promena lozinke vašeg korisničkog naloga" />
      <Card withBorder radius="md" p="lg" maw={480}>
        <form
          onSubmit={form.onSubmit((values) =>
            mutation.mutate({ oldPassword: values.oldPassword, newPassword: values.newPassword }),
          )}
        >
          <Stack>
            <PasswordInput label="Stara lozinka" autoComplete="current-password" {...form.getInputProps('oldPassword')} />
            <PasswordInput label="Nova lozinka" autoComplete="new-password" {...form.getInputProps('newPassword')} />
            <PasswordInput
              label="Potvrda nove lozinke"
              autoComplete="new-password"
              {...form.getInputProps('confirmPassword')}
            />
            <Button type="submit" loading={mutation.isPending} mt="sm">
              Sačuvaj
            </Button>
          </Stack>
        </form>
      </Card>
    </>
  );
}
