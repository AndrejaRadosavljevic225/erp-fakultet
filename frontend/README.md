# ERP Fakultet — frontend

Web aplikacija (React + TypeScript) nad mikroservisima `hr-service` i `schedule-service`, kojima se
pristupa preko `api-gateway`-a.

## Stack

| Sloj | Biblioteka |
|---|---|
| Build | Vite + React 19 + TypeScript |
| UI | Mantine v9 (`@mantine/core`, `dates`, `form`, `modals`, `notifications`, `charts`) |
| Server state | TanStack Query v5 |
| Rutiranje | react-router-dom v7 |
| HTTP | axios (Bearer token interceptor) |
| Kalendar | FullCalendar v6 (timeGrid / dayGrid) |

## Najbrže — ceo sistem u jednom kliku

Dupli klik na `start.bat` u korenu repozitorijuma (ili `docker compose --profile app up -d --build --wait`).
Aplikacija je onda na `http://localhost:3000`, zajedno sa celim backendom.

## Pokretanje u razvoju

1. Pokreni backend (iz korena repozitorijuma):

```bash
docker compose --profile app up -d --build --wait
```

2. Pokreni frontend:

```bash
npm install && npm run dev
```

Aplikacija je na `http://localhost:5173`, a gateway na `http://localhost:8080`
(podesivo kroz `VITE_API_URL` u `.env`).

Nalozi koje seeder upisuje na svežu bazu: **admin / admin123**, **hr / hr1234**,
**profesor / prof1234**. Prijava radi i korisničkim imenom i email adresom zaposlenog.

## Skripte

```bash
npm run dev       # razvojni server
npm run build     # tsc -b + produkcijski build u dist/
npm run lint      # oxlint
npm run preview   # lokalni pregled produkcijskog build-a
```

## Docker

Frontend ima svoj `Dockerfile` (nginx) i deo je `app` profila u `docker-compose.yml`:

```bash
docker compose --profile app up -d --build
```

Nakon toga je aplikacija na `http://localhost:3000`. U kontejneru nginx proksira `/hr` i `/schedule`
na gateway, pa frontend i API dele isti origin (nema CORS-a) i `VITE_API_URL` ostaje prazan.

## Struktura

```
src/
  api/         axios klijent i pozivi po servisu (hr.ts, schedule.ts)
  types/       TypeScript tipovi 1:1 sa backend DTO-ovima
  auth/        AuthContext, ProtectedRoute, RoleGate
  components/  Layout, zaglavlje strane, tabela/paginacija, polja za datum
  lib/         formatiranje, prevodi enum vrednosti, notifikacije
  features/    ekrani po modulima: auth, dashboard, hr, schedule, teaching, admin
```

## Pristup po rolama

Backend odlučuje preko `@PreAuthorize`; frontend prikazuje samo ono što je dozvoljeno.

| | ADMIN | HR | PROFESOR |
|---|---|---|---|
| Zaposleni | sve | sve osim brisanja | pregled |
| Radna mesta / Prostorije / Školske godine | sve | sve osim brisanja | pregled |
| Korisnički nalozi | sve | sve osim brisanja | — |
| Role / Permisije / Istorija izmena | sve | pregled | — |
| Rezervacije | sve + odobravanje | sve + odobravanje | svoje |
| Norme i dodele nastavnika | sve | sve | — |
| Fond časova | svi | svi | svoj |

## Pokrivenost use-case-ova

| Use-case | Ekran |
|---|---|
| UC-G-01 Prijava | `/login` |
| UC-G-02 Role i permisije | `/roles`, `/permissions` |
| UC-G-03 Audit log | `/audit-logs` |
| UC-HR-01 Registracija zaposlenog | `/workers` |
| UC-HR-02 Korisnički nalozi | `/users` |
| UC-HR-03 Fond časova | `/teaching` |
| UC-HR-05 Radna mesta | `/positions`, profil zaposlenog |
| UC-HR-06 Status zaposlenog | profil zaposlenog → „Promena statusa” |
| UC-SC-01 Prostorije | `/rooms` |
| UC-SC-02 Rezervacija + provera zauzeća | `/bookings` → „Nova rezervacija” |
| UC-SC-03 Odobravanje | `/approvals` |
| UC-SC-04 Otkazivanje | `/bookings` |
| UC-SC-05 Pregled zauzetosti | `/calendar` |

Finansijski modul (UC-FN-01…06) i obračun plate (UC-HR-04) nisu pokriveni jer `finance-service`
još nije implementiran.
