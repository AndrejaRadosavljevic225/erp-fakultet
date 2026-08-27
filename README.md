# ERP Fakultet

Informacioni sistem za upravljanje kadrovima i prostorijama fakulteta, rađen kao praktični deo
master rada. Sastoji se od Spring Boot mikroservisa iza API gateway-a i React web aplikacije,
a ceo sistem se podiže jednom komandom u Docker-u.

## Pokretanje

Dupli klik na **`start.bat`** — skripta pokreće Docker ako ne radi, podiže sve servise, čeka da
budu spremni i otvara aplikaciju. Zaustavljanje: **`stop.bat`**.

Isto iz terminala:

```bash
docker compose --profile app up -d --build --wait
```

Aplikacija je na **http://localhost:3000**. Prvo pokretanje traje nekoliko minuta jer se grade
slike; kasnije je ispod pola minuta.

Na praznoj bazi se sami upisuju početni podaci, pa je sistem odmah upotrebljiv:

| Korisnik | Lozinka | Rola |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `hr` | `hr1234` | HR |
| `profesor` | `prof1234` | PROFESOR |

Prijava radi i email adresom zaposlenog (npr. `petar.petrovic@fakultet.rs`).

Detaljno uputstvo, uključujući rad iz IntelliJ-a: [DEV_SETUP.md](DEV_SETUP.md).

## Šta sistem radi

**Kadrovi (HR)** — evidencija zaposlenih sa pretragom, profil sa istorijom izmena, promena
statusa (odsustvo, suspenzija, prestanak radnog odnosa), radna mesta i dodela zaposlenih na njih,
korisnički nalozi, role i permisije, audit log svih izmena.

**Raspored** — prostorije, rezervacije sa **proverom preklapanja pre slanja**, odobravanje i
odbijanje zahteva, otkazivanje, i kalendar zauzetosti sa filterima po sali, zgradi i kapacitetu.

**Fond časova** — norma časova po zvanju za školsku godinu, dodela nastavnika, i izveštaj koji
poredi normu sa realizovanim satima iz održanih nastavnih termina (odstupanje, prekovremeni sati).

Pristup je ograničen rolom, i to na serveru: ADMIN ima sve, HR sve osim brisanja i upravljanja
rolama, PROFESOR čita zajedničke podatke i barata samo svojim rezervacijama i svojim fondom časova.

## Arhitektura

```
                    ┌──────────────┐
   pregledač  ───►  │   frontend   │  React + TypeScript, nginx
                    │   :3000      │  (servira build, proksira /hr i /schedule)
                    └──────┬───────┘
                           ▼
                    ┌──────────────┐
                    │ api-gateway  │  jedan ulaz, prosleđuje JWT
                    │   :8080      │
                    └──┬────────┬──┘
              /hr/**   │        │   /schedule/**
                       ▼        ▼
        ┌────────────────┐   ┌──────────────────┐
        │  hr-service    │   │ schedule-service │
        │  :8081         │   │ :8083            │
        └────────┬───────┘   └────────┬─────────┘
                 ▼                    ▼
            ┌─────────┐          ┌──────────────┐
            │ erp_hr  │          │ erp_schedule │   PostgreSQL
            └─────────┘          └──────────────┘
```

Svaki servis ima sopstvenu bazu. Prijava se obavlja u `hr-service`-u, koji izdaje JWT; oba
servisa ga validiraju istim tajnim ključem, pa `schedule-service` nema sopstvenu bazu korisnika.

| Modul | Uloga |
|---|---|
| `frontend` | React 19 + TypeScript + Mantine, TanStack Query, FullCalendar |
| `api-gateway` | Spring Cloud Gateway — rutiranje i CORS |
| `hr-service` | zaposleni, radna mesta, nalozi, role, permisije, audit log, prijava |
| `schedule-service` | prostorije, rezervacije, školske godine, norme, fond časova |
| `erp-common` | zajednički DTO-ovi, obrada grešaka, pomoćne klase |
| `finance-service` | **nije implementiran** — projektovan, van opsega ovog rada |

**Stack:** Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL 16,
Maven (multi-modul), Docker Compose; React 19, TypeScript, Vite, Mantine.

## Testovi

```bash
./mvnw test     # ili dupli klik na test.bat, koji sam pronađe JDK 21
```

44 testa, bez Docker-a i baze (H2 u memoriji). Pokrivena su poslovna pravila koja nose sistem:
preklapanje termina sa graničnim slučajevima, računica fonda časova, prijava korisničkim imenom
i email-om, i autorizacija po rolama nad HTTP slojem.

Sistemski test nad pokrenutim sistemom (85 provera kroz gateway i pravu bazu):

```bash
node tools/e2e/api-test.mjs
```

## Obim rada i ograničenja

Rad pokriva **kadrovsku evidenciju i upravljanje prostorijama**; finansijski modul je projektovan
(model podataka, use-case-ovi, ruta na gateway-u) ali nije implementiran i predstavlja dalji rad.
Iz istog razloga u sistemu nema asinhrone komunikacije — jedini tokovi koji bi je opravdali
(knjiženje transakcija i obračun plata) pripadaju finansijskom modulu.

Sistem je namenjen radu u lokalnom, kontejnerizovanom okruženju. Za produkciju bi bilo potrebno:
migracije šeme umesto `ddl-auto`, upravljanje tajnama izvan repozitorijuma, HTTPS i orkestrator
umesto Docker Compose-a.

## Struktura repozitorijuma

```
erp-fakultet/
├── start.bat / stop.bat / test.bat   pokretanje, zaustavljanje, testovi
├── docker-compose.yml                ceo sistem (profil "app")
├── api-gateway/                      ulazna tačka, rutiranje
├── hr-service/                       kadrovi, nalozi, RBAC
├── schedule-service/                 prostorije, rezervacije, fond časova
├── finance-service/                  nije implementiran
├── erp-common/                       zajedničke klase
├── frontend/                         React aplikacija
├── tools/e2e/                        sistemski test API-ja
├── docker/postgres/                  inicijalizacija baza
└── DEV_SETUP.md                      detaljno uputstvo za pokretanje
```

## Licenca

Apache License 2.0 — vidi [LICENSE](LICENSE).
