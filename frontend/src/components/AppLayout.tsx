import { AppShell, Avatar, Burger, Group, Menu, NavLink, ScrollArea, Text, Title, UnstyledButton } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import {
  IconBriefcase,
  IconBuilding,
  IconCalendarEvent,
  IconCalendarStats,
  IconChartBar,
  IconChecklist,
  IconHistory,
  IconKey,
  IconLayoutDashboard,
  IconLogout,
  IconPassword,
  IconShieldLock,
  IconUserCog,
  IconUsers,
} from '@tabler/icons-react';
import { NavLink as RouterNavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { roleLabels } from '../lib/labels';

interface NavItem {
  to: string;
  label: string;
  icon: typeof IconUsers;
  /** Ako je zadato, stavka se prikazuje samo tim rolama. */
  roles?: string[];
}

interface NavSection {
  title: string;
  items: NavItem[];
}

const SECTIONS: NavSection[] = [
  {
    title: 'Pregled',
    items: [{ to: '/', label: 'Početna', icon: IconLayoutDashboard }],
  },
  {
    title: 'Kadrovi',
    items: [
      { to: '/workers', label: 'Zaposleni', icon: IconUsers },
      { to: '/positions', label: 'Radna mesta', icon: IconBriefcase, roles: ['ADMIN', 'HR'] },
    ],
  },
  {
    title: 'Raspored',
    items: [
      { to: '/rooms', label: 'Prostorije', icon: IconBuilding },
      { to: '/bookings', label: 'Rezervacije', icon: IconCalendarEvent },
      { to: '/calendar', label: 'Zauzetost', icon: IconCalendarStats },
      { to: '/approvals', label: 'Odobravanje', icon: IconChecklist, roles: ['ADMIN', 'HR'] },
    ],
  },
  {
    title: 'Nastava',
    items: [
      { to: '/school-years', label: 'Školske godine', icon: IconCalendarStats, roles: ['ADMIN', 'HR'] },
      { to: '/teaching', label: 'Fond časova', icon: IconChartBar },
    ],
  },
  {
    title: 'Administracija',
    items: [
      { to: '/users', label: 'Korisnički nalozi', icon: IconUserCog, roles: ['ADMIN', 'HR'] },
      { to: '/roles', label: 'Role', icon: IconShieldLock, roles: ['ADMIN', 'HR'] },
      { to: '/permissions', label: 'Permisije', icon: IconKey, roles: ['ADMIN', 'HR'] },
      { to: '/audit-logs', label: 'Istorija izmena', icon: IconHistory, roles: ['ADMIN', 'HR'] },
    ],
  },
];

export function AppLayout() {
  const [opened, { toggle, close }] = useDisclosure();
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const initials = (user?.workerFullName ?? user?.username ?? '?')
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  return (
    <AppShell
      header={{ height: 60 }}
      navbar={{ width: 260, breakpoint: 'sm', collapsed: { mobile: !opened } }}
      padding="md"
    >
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between" wrap="nowrap">
          <Group gap="sm" wrap="nowrap">
            <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
            <Title order={4}>ERP Fakultet</Title>
          </Group>

          <Menu position="bottom-end" withArrow>
            <Menu.Target>
              <UnstyledButton>
                <Group gap="xs" wrap="nowrap">
                  <Avatar color="blue" radius="xl" size={32}>
                    {initials}
                  </Avatar>
                  <div style={{ lineHeight: 1.2 }} className="mantine-visible-from-sm">
                    <Text size="sm" fw={500}>
                      {user?.workerFullName ?? user?.username}
                    </Text>
                    <Text size="xs" c="dimmed">
                      {user?.roleCode ? (roleLabels[user.roleCode] ?? user.roleName ?? user.roleCode) : 'Bez role'}
                    </Text>
                  </div>
                </Group>
              </UnstyledButton>
            </Menu.Target>
            <Menu.Dropdown>
              <Menu.Label>{user?.username}</Menu.Label>
              <Menu.Item leftSection={<IconPassword size={16} />} onClick={() => navigate('/change-password')}>
                Promena lozinke
              </Menu.Item>
              <Menu.Divider />
              <Menu.Item color="red" leftSection={<IconLogout size={16} />} onClick={handleLogout}>
                Odjava
              </Menu.Item>
            </Menu.Dropdown>
          </Menu>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="xs">
        <ScrollArea>
          {SECTIONS.map((section) => {
            const visible = section.items.filter((item) => !item.roles || hasRole(...item.roles));
            if (visible.length === 0) return null;
            return (
              <div key={section.title}>
                <Text size="xs" fw={700} c="dimmed" tt="uppercase" px="sm" mt="sm" mb={4}>
                  {section.title}
                </Text>
                {visible.map((item) => (
                  <NavLink
                    key={item.to}
                    component={RouterNavLink}
                    to={item.to}
                    label={item.label}
                    leftSection={<item.icon size={18} stroke={1.5} />}
                    active={item.to === '/' ? location.pathname === '/' : location.pathname.startsWith(item.to)}
                    onClick={close}
                  />
                ))}
              </div>
            );
          })}
        </ScrollArea>
      </AppShell.Navbar>

      <AppShell.Main>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
}
