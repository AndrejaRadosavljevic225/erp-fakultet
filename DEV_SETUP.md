# ERP Fakultet — pokretanje

## Preduslovi
- **Docker Desktop** (za sve; opcija A)
- Za rad iz IDE-a (opcija B): **JDK 21** + Maven 3.9+ (ima `mvnw` wrapper)

---

## Opcija A — jedan klik (preporuka)

Dupli klik na **`start.bat`** u korenu projekta. Skripta pokreće Docker ako ne radi,
podiže ceo sistem, čeka da svi servisi budu **zdravi** i otvara aplikaciju.
Zaustavljanje: **`stop.bat`**.

Isto to iz terminala:
```bash
docker compose --profile app up -d --build --wait
```
Podiže bazu, 3 servisa i frontend. Zahvaljujući healthcheck-ovima, `--wait`
se završava tek kad je sistem stvarno spreman za prijavu (bez toga prve prijave vraćaju 500
dok se `hr-service` diže). Prvi build traje par minuta; kasnije bez `--build` je brzo.

| Adresa | Šta je |
|---|---|
| http://localhost:3000 | **aplikacija (frontend)** |
| http://localhost:8080 | api-gateway |

**Nalozi na svežoj bazi** (upisuje ih seeder pri prvom pokretanju):

| Korisnik | Lozinka | Rola |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `hr` | `hr1234` | HR |
| `profesor` | `prof1234` | PROFESOR |

Prijava radi i email adresom zaposlenog (npr. `petar.petrovic@fakultet.rs`).
Lozinka administratora se može promeniti preko `ADMIN_PASSWORD`, odn. `app.admin-password`.

Gašenje: `docker compose --profile app down`  •  + brisanje podataka: `... down -v`

## Opcija B — samo baza u Dockeru, servisi iz IntelliJ-a

```bash
docker compose up -d          # samo Postgres
```
Pa iz IntelliJ-a (ili terminala) pokreni servise:
```bash
mvn -q -pl erp-common -am install     # jednom
mvn -pl hr-service spring-boot:run
mvn -pl schedule-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

UI alat (pgAdmin :5050): `docker compose --profile tools up -d`

---

## Servisi i portovi

| Servis | Port | Baza |
|---|---|---|
| **api-gateway** | 8080 | — (jedan ulaz za sve) |
| hr-service | 8081 | `erp_hr` |
| schedule-service | 8083 | `erp_schedule` |
| finance-service | 8082 | `erp_finance` (još nije implementiran) |

Postgres: `localhost:5432` (`erp`/`erp`)

## Pristup preko gateway-a (jedan URL)

- HR:       `http://localhost:8080/hr/api/...`       → hr-service
- Schedule: `http://localhost:8080/schedule/api/...` → schedule-service

**Auth:** `POST /hr/api/auth/login` → `data.token` → šalji `Authorization: Bearer <token>`.
Difoltni nalog: `admin` / `admin123` (rola ADMIN). Role: ADMIN / HR / PROFESOR.

## Početni podaci (seeder)

Na **praznoj** bazi servisi sami upisuju početne podatke, da sistem odmah bude upotrebljiv:

- `DefaultRoleSeeder` (HR) — role ADMIN/HR/PROFESOR i administratorski nalog
- `DemoDataSeeder` (HR) — 5 zaposlenih, 4 radna mesta, nalozi `hr` i `profesor`, permisije
- `DemoDataSeeder` (Schedule) — 4 prostorije, tekuća školska godina, norma od 12 časova,
  dodele nastavnika i 5 termina u tekućoj nedelji (3 odobrena + 2 koja čekaju odobrenje)

Seeder-i se pokreću samo kad je odgovarajuća tabela prazna, pa ponovno pokretanje ne duplira
podatke. Demonstracioni podaci se isključuju sa `app.demo-data=false` (tako je u `prod` profilu).

## Testovi

Dupli klik na **`test.bat`** (sam pronalazi JDK 21) ili iz terminala:

```bash
./mvnw test          # 44 testa, bez Dockera i baze (H2 u memoriji)
```

> Maven mora da radi na **JDK 21**. Ako je sistemski `java` stariji, `mvn` puca sa
> `UnsupportedClassVersionError ... class file version 61.0`. Tada postavi `JAVA_HOME`
> na JDK 21 ili koristi `test.bat`, koji to radi umesto tebe.

| Test | Šta pokriva |
|---|---|
| `BookingRepositoryTest` | preklapanje termina i zauzetost — granični slučajevi (dodirivanje krajeva, obuhvatanje, otkazan termin, više sala) |
| `TeachingServiceImplTest` | fond časova: norma vs realizovano, prekovremeni, izuzimanje nenastavnih i neodobrenih termina, zabrana uvida u tuđi fond |
| `AuthControllerTest` | prijava imenom i email-om, pogrešna lozinka, `/auth/me` sa tokenom i bez njega |
| `WorkerControllerSecurityTest` | autorizacija po rolama nad HTTP slojem: ADMIN sve, HR bez brisanja, PROFESOR samo čitanje |

Sistemski test nad pokrenutim sistemom (85 provera kroz gateway i pravu bazu):
`node tools/e2e/api-test.mjs` — detalji u [tools/e2e/README.md](tools/e2e/README.md).

## Konfiguracija (env varijable, dev fallback)

- `SPRING_PROFILES_ACTIVE` (dev/prod), `JWT_SECRET`, `DB_HOST/DB_NAME/DB_USER/DB_PASSWORD`
- Gateway rute: `HR_URI`, `SCHEDULE_URI`, `FINANCE_URI`
- Profili: `dev` = `ddl-auto=update` + SQL log; `prod` = `ddl-auto=validate`, bez logova.
