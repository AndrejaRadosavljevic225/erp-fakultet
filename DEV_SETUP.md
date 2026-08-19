# ERP Fakultet — pokretanje

## Preduslovi
- **Docker Desktop** (za sve; opcija A)
- Za rad iz IDE-a (opcija B): **JDK 21** + Maven 3.9+ (ima `mvnw` wrapper)

---

## Opcija A — CEO STACK jednom komandom (preporuka)

Podiže bazu + Kafku + sva 3 servisa u kontejnerima:
```bash
docker compose --profile app up -d --build
```
Prvi put build traje par minuta (gradi slike). Kasnije bez `--build` je brzo.

Gašenje: `docker compose --profile app down`  •  + brisanje podataka: `... down -v`

## Opcija B — samo baza u Dockeru, servisi iz IntelliJ-a

```bash
docker compose up -d          # samo Postgres + Kafka
```
Pa iz IntelliJ-a (ili terminala) pokreni servise:
```bash
mvn -q -pl erp-common -am install     # jednom
mvn -pl hr-service spring-boot:run
mvn -pl schedule-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

UI alati (pgAdmin :5050, kafka-ui :8090): `docker compose --profile tools up -d`

---

## Servisi i portovi

| Servis | Port | Baza |
|---|---|---|
| **api-gateway** | 8080 | — (jedan ulaz za sve) |
| hr-service | 8081 | `erp_hr` |
| schedule-service | 8083 | `erp_schedule` |
| finance-service | 8082 | `erp_finance` (još nije implementiran) |

Postgres: `localhost:5432` (`erp`/`erp`) • Kafka: `localhost:9092`

## Pristup preko gateway-a (jedan URL)

- HR:       `http://localhost:8080/hr/api/...`       → hr-service
- Schedule: `http://localhost:8080/schedule/api/...` → schedule-service

**Auth:** `POST /hr/api/auth/login` → `data.token` → šalji `Authorization: Bearer <token>`.
Difoltni nalog: `admin` / `admin123` (rola ADMIN). Role: ADMIN / HR / PROFESOR.

## Konfiguracija (env varijable, dev fallback)

- `SPRING_PROFILES_ACTIVE` (dev/prod), `JWT_SECRET`, `DB_HOST/DB_NAME/DB_USER/DB_PASSWORD`, `KAFKA_SERVERS`
- Gateway rute: `HR_URI`, `SCHEDULE_URI`, `FINANCE_URI`
- Profili: `dev` = `ddl-auto=update` + SQL log; `prod` = `ddl-auto=validate`, bez logova.
