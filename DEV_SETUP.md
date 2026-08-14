# ERP Fakultet — lokalno okruzenje (dev)

## Preduslovi
- **JDK 21** (obavezno — projekat je Spring Boot 4 / Java 21). Proveri: `java -version` mora da pise 21.
- **Maven 3.9+** (ima i `mvnw` wrapper u repo-u)
- **Docker Desktop** (za bazu i Kafku)

> Napomena: ako `java -version` pokazuje 1.8, instaliraj JDK 21 (npr. Temurin 21) i podesi
> `JAVA_HOME` na taj JDK, pa restartuj terminal.

## 1) Podigni infrastrukturu (Postgres + Kafka)
```bash
docker compose up -d
```
Sa UI alatima (pgAdmin na :5050, kafka-ui na :8090):
```bash
docker compose --profile tools up -d
```
Gasenje: `docker compose down`  •  Gasenje + brisanje podataka: `docker compose down -v`

## 2) Servisi i portovi
| Servis           | Port | Baza          | Swagger                          |
|------------------|------|---------------|----------------------------------|
| api-gateway      | 8080 | —             | —                                |
| hr-service       | 8081 | `erp_hr`      | http://localhost:8081/swagger-ui.html |
| finance-service  | 8082 | `erp_finance` | http://localhost:8082/swagger-ui.html |
| schedule-service | 8083 | `erp_schedule`| http://localhost:8083/swagger-ui.html |

Postgres: `localhost:5432`, korisnik `erp`, lozinka `erp`.
Kafka (za servise na host masini): `localhost:9092`.

## 3) Build i pokretanje servisa
Prvo napravi `erp-common` (ostali zavise od njega):
```bash
mvn -q -pl erp-common -am install
```
Pokretanje pojedinacnog servisa (iz root foldera):
```bash
mvn -pl hr-service spring-boot:run
```

## Napomene / TODO
- Sema baze se za sad pravi automatski iz JPA entiteta (`ddl-auto=update`).
  Flyway je iskljucen jer su migracije prazne — kasnije popuniti `V1__*.sql` i preci na `validate`.
- Poslovna logika (kontroleri, servisi, security) je vecinom prazna — tek treba implementirati.
- Rute na api-gateway-u su zakomentarisane; do implementacije gadjaj servise direktno na portovima.
