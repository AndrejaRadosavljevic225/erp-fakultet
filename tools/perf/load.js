// Test opterecenja: tipican rad korisnika u aplikaciji (citanje).
//
// Scenario jedne iteracije prati ono sto korisnik stvarno radi: otvori listu
// zaposlenih, prostorije, zahteve koji cekaju odobrenje, kalendar zauzetosti,
// proveri da li je termin slobodan i pogleda fond casova.
//
// Pokretanje (sistem mora biti podignut):
//   docker run --rm --network erp-fakultet_default -v "%cd%/tools/perf:/perf" grafana/k6 run /perf/load.js
import { sleep } from 'k6';
import { prijava, get, post, interval, intervalDana, BASE } from './lib.js';
import http from 'k6/http';

export const options = {
  stages: [
    { duration: '20s', target: 10 }, // postepeno do 10 istovremenih korisnika
    { duration: '40s', target: 10 }, // odrzavanje opterecenja
    { duration: '10s', target: 0 },  // smirivanje
  ],
  thresholds: {
    // Ciljevi kvaliteta: 95% zahteva ispod 500 ms i manje od 1% gresaka.
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],

    // Isti cilj po pojedinacnom pozivu — da se vidi koji je najskuplji.
    'http_req_duration{naziv:prijava}': ['p(95)<500'],
    'http_req_duration{naziv:lista zaposlenih}': ['p(95)<500'],
    'http_req_duration{naziv:lista prostorija}': ['p(95)<500'],
    'http_req_duration{naziv:zahtevi na cekanju}': ['p(95)<500'],
    'http_req_duration{naziv:kalendar zauzetosti}': ['p(95)<500'],
    'http_req_duration{naziv:provera dostupnosti}': ['p(95)<500'],
    'http_req_duration{naziv:fond casova}': ['p(95)<500'],
  },
};

/** Jednom pre testa: prijava i prikupljanje identifikatora nad kojima se radi. */
export function setup() {
  const headers = prijava();

  const sale = http.get(`${BASE}/schedule/api/rooms?size=50`, { headers }).json('data.content');
  const salaId = sale && sale.length ? sale[0].id : null;

  const godine = http.get(`${BASE}/schedule/api/school-years?size=10`, { headers }).json('data.content');
  let fond = null;
  for (const godina of godine || []) {
    const dodele = http.get(`${BASE}/schedule/api/teaching/assignments/year/${godina.id}`, { headers }).json('data');
    if (dodele && dodele.length) {
      fond = { workerId: dodele[0].workerId, schoolYearId: godina.id };
      break;
    }
  }
  return { salaId, fond };
}

export default function (podaci) {
  const headers = prijava();

  get('/hr/api/workers?page=0&size=20', headers, 'lista zaposlenih');
  get('/schedule/api/rooms?page=0&size=20', headers, 'lista prostorija');
  get('/schedule/api/bookings/status/REQUESTED?page=0&size=20', headers, 'zahtevi na cekanju');

  const nedelja = intervalDana(0, 7);
  get(
    `/schedule/api/bookings/occupancy?from=${nedelja.startDateTime}&to=${nedelja.endDateTime}`,
    headers,
    'kalendar zauzetosti',
  );

  if (podaci.salaId) {
    post(
      '/schedule/api/bookings/availability',
      { roomId: podaci.salaId, ...interval(7, 10) },
      headers,
      'provera dostupnosti',
    );
  }

  if (podaci.fond) {
    get(
      `/schedule/api/teaching/report?workerId=${podaci.fond.workerId}&schoolYearId=${podaci.fond.schoolYearId}`,
      headers,
      'fond casova',
    );
  }

  sleep(1); // pauza izmedju koraka, kao kod stvarnog korisnika
}
