# Sistemski (end-to-end) test API-ja

`api-test.mjs` prolazi kroz **ceo sistem u pogonu** — kroz `api-gateway`, oba mikroservisa i
pravu Postgres bazu — i proverava 85 tvrdnji: prijavu (imenom, email-om, pogrešnom lozinkom),
autorizaciju po rolama, CRUD nad zaposlenima i radnim mestima, korisničke naloge, role,
permisije, audit log, prostorije, rezervacije sa proverom preklapanja, odobravanje i
otkazivanje, zauzetost sa filterima i fond časova.

Za razliku od JUnit testova (koji rade nad bazom u memoriji i pokreću se sa `mvn test`),
ovaj test zahteva **pokrenut sistem** i proverava i gateway, i rutiranje, i pravu bazu.

## Pokretanje

```bash
docker compose --profile app up -d --build --wait
node tools/e2e/api-test.mjs
```

Podrazumevani cilj je `http://localhost:8080`; drugi se zadaje preko `BASE`:

```bash
BASE=http://localhost:3000 node tools/e2e/api-test.mjs
```

(Kroz port 3000 ide isti API, samo preko nginx proxy-ja u frontend kontejneru.)

## Šta ostaje posle testa

Test na kraju briše sve što je napravio i proverava da obrisani zapis zaista više ne postoji.
Izlaz je spisak `PASS`/`FAIL` po tvrdnji i zbirni rezultat; izlazni kod je različit od nule
ako ijedna provera padne, pa se može koristiti i u automatizaciji.

Potreban je samo Node 18+ (koristi ugrađeni `fetch`, bez dodatnih paketa).
