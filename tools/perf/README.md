# Testovi performansi (k6)

Tri scenarija nad **pokrenutim sistemom**, kroz `api-gateway` i pravu bazu:

| Skripta | Šta meri |
|---|---|
| `load.js` | tipičan rad korisnika — čitanje: liste, kalendar zauzetosti, provera dostupnosti, fond časova |
| `write.js` | putanju upisa — kreiranje rezervacije (sa proverom preklapanja) i otkazivanje |
| `stress.js` | ponašanje pri postepenom povećanju do 100 istovremenih korisnika |

Svaki scenario ima **pragove** (`thresholds`) koji su ujedno i kriterijum prolaza: 95% zahteva
ispod zadatog vremena i manje od 1% grešaka. Ako prag padne, k6 se završava neuspehom, pa se
scenariji mogu koristiti i u automatizaciji.

## Pokretanje

Prvo podigni sistem, pa pusti scenario. Nije potrebna instalacija — k6 se pokreće iz kontejnera
na istoj mreži kao i servisi:

```bash
docker compose --profile app up -d --wait
```

```bash
docker run --rm --network erp-fakultet_default -v "%cd%/tools/perf:/perf" grafana/k6 run /perf/load.js
```

Isto za `/perf/write.js` i `/perf/stress.js`. U Git Bash-u dodaj `MSYS_NO_PATHCONV=1` ispred
komande i navedi punu putanju, jer inače pokušava da prevede `/perf` u Windows putanju.

Cilj se može promeniti promenljivom `BASE` (npr. `-e BASE=http://frontend:80` za merenje kroz
nginx, ili adresa udaljenog servera).

## Izmereni rezultati

Mereno na razvojnoj mašini, sa celim sistemom u Docker-u (PostgreSQL, dva servisa, gateway) i
k6 kontejnerom na istoj mreži. Brojevi zato pokazuju **odnose i ponašanje pod opterećenjem**,
ne kapacitet servera.

### Opterećenje čitanjem — `load.js`

10 istovremenih korisnika, 70 sekundi.

| Mera | Vrednost |
|---|---|
| Zahteva | 3.518 (49,8 u sekundi) |
| Trajanje zahteva, p95 | **78 ms** (prag: 500 ms) |
| Greške | **0%** |
| Neuspele provere | 0 od 3.518 |

Po pojedinačnom pozivu (p95):

| Poziv | p95 | prosek |
|---|---|---|
| prijava | 104 ms | 85 ms |
| lista zaposlenih | 8,0 ms | 5,7 ms |
| kalendar zauzetosti | 7,8 ms | 5,6 ms |
| zahtevi na čekanju | 7,4 ms | 5,0 ms |
| provera dostupnosti | 6,3 ms | 4,4 ms |
| fond časova | 6,3 ms | 4,6 ms |
| lista prostorija | 5,7 ms | 3,9 ms |

**Zapažanje:** prijava je oko **15 puta skuplja** od svih ostalih poziva. To nije propust nego
posledica BCrypt algoritma, koji je namerno spor da bi otežao napad grubom silom. Upiti nad
bazom — uključujući proveru preklapanja termina i računanje fonda časova — drže se u jednocifrenim
milisekundama.

### Opterećenje upisom — `write.js`

5 istovremenih korisnika, 55 sekundi, svaka iteracija kreira i odmah otkazuje rezervaciju.

| Mera | Vrednost |
|---|---|
| Zahteva | 6.026 (109 u sekundi) |
| Kreiranih i otkazanih rezervacija | 2.008 |
| Trajanje zahteva, p95 | **95 ms** (prag: 800 ms) |
| Greške | **0%** |

Upis prolazi kroz proveru preklapanja u bazi i svejedno ostaje ispod praga.

### Izdržljivost — `stress.js`

Postepeno do 100 istovremenih korisnika.

| Mera | Vrednost |
|---|---|
| Zahteva | 8.742 (124 u sekundi) |
| Trajanje zahteva, p95 | 673 ms |
| Najduži zahtev | 1,32 s |
| Greške | **0%** |

Sistem izdržava 100 istovremenih korisnika bez ijedne greške; vreme odziva raste sa ~80 ms na
~670 ms (p95), što je očekivano jer sve radi na jednoj mašini. Prelomna tačka nije dostignuta —
nema odbijenih zahteva ni isteka veze.

## Napomene

- Svaka iteracija radi **novu prijavu**, što je najgori slučaj; stvarni korisnik se prijavi jednom
  i dalje koristi isti token, pa su prosečna vremena u praksi niža.
- `write.js` ostavlja otkazane rezervacije u bazi (otkazivanje je logičko, nema brisanja termina).
  Posle većeg broja pokretanja baza se može očistiti sa `docker compose --profile app down -v`.
- Merenja idu na `api-gateway`, pa ne uključuju nginx koji servira frontend; za merenje kroz njega
  koristi `-e BASE=http://frontend:80`.
