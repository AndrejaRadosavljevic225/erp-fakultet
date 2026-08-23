import { useState } from 'react';
import { Alert, Button, Card, Center, PasswordInput, Stack, Text, TextInput, Title } from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconAlertCircle } from '@tabler/icons-react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { errorMessage } from '../../api/client';

export function LoginPage() {
  const { login, user, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const form = useForm({
    initialValues: { usernameOrEmail: '', password: '' },
    validate: {
      usernameOrEmail: (value) => (value.trim() ? null : 'Unesite korisničko ime ili email'),
      password: (value) => (value ? null : 'Unesite lozinku'),
    },
  });

  if (!loading && user) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = form.onSubmit(async (values) => {
    setError(null);
    setSubmitting(true);
    try {
      await login(values.usernameOrEmail.trim(), values.password);
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from && from !== '/login' ? from : '/', { replace: true });
    } catch (err) {
      setError(errorMessage(err, 'Prijava nije uspela'));
    } finally {
      setSubmitting(false);
    }
  });

  return (
    <Center mih="100vh" p="md">
      <Card withBorder shadow="md" radius="md" p="xl" w={400} maw="100%">
        <Stack gap="lg">
          <Stack gap={4}>
            <Title order={2}>ERP Fakultet</Title>
            <Text c="dimmed" size="sm">
              Prijavite se korisničkim imenom ili email adresom
            </Text>
          </Stack>

          {error && (
            <Alert color="red" icon={<IconAlertCircle size={18} />}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <Stack>
              <TextInput
                label="Korisničko ime ili email"
                placeholder="admin"
                autoFocus
                autoComplete="username"
                {...form.getInputProps('usernameOrEmail')}
              />
              <PasswordInput
                label="Lozinka"
                placeholder="Vaša lozinka"
                autoComplete="current-password"
                {...form.getInputProps('password')}
              />
              <Button type="submit" loading={submitting} fullWidth mt="sm">
                Prijava
              </Button>
            </Stack>
          </form>
        </Stack>
      </Card>
    </Center>
  );
}
