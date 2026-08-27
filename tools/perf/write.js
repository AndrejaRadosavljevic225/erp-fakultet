// Test opterecenja putanje UPISA: kreiranje rezervacije (sa proverom preklapanja
// u bazi) i njeno otkazivanje.
//
// Svaki virtuelni korisnik uzima svoj termin, pa se ne sudaraju medjusobno;
// termin se odmah otkazuje, cime prestaje da zauzima salu.
//
// Pokretanje (sistem mora biti podignut):
//   docker run --rm --network erp-fakultet_default -v "%cd%/tools/perf:/perf" grafana/k6 run /perf/write.js
import http from 'k6/http';
import { check } from 'k6';
import { prijava, post, BASE } from './lib.js';

export const options = {
  stages: [
    { duration: '15s', target: 5 },
    { duration: '30s', target: 5 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    // Upis ukljucuje proveru preklapanja i upis u bazu, pa je granica blaza nego za citanje.
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],
  },
};

export function setup() {
  const headers = prijava();
  const sale = http.get(`${BASE}/schedule/api/rooms?size=50`, { headers }).json('data.content');
  const dostupne = (sale || []).filter((s) => s.bookable && s.active);
  return { salaId: dostupne.length ? dostupne[0].id : sale[0].id };
}

export default function (podaci) {
  const headers = prijava();

  // Jedinstven termin po (virtuelni korisnik, iteracija) — bez sudara sa drugima.
  const redni = __VU * 1000 + __ITER;
  const dan = new Date();
  dan.setDate(dan.getDate() + 200 + Math.floor(redni / 12));
  const datum = dan.toISOString().slice(0, 10);
  const sat = 7 + (redni % 12);
  const dva = (n) => String(n).padStart(2, '0');

  const kreirana = post(
    '/schedule/api/bookings',
    {
      roomId: podaci.salaId,
      requesterWorkerId: 1,
      startDateTime: `${datum}T${dva(sat)}:00:00`,
      endDateTime: `${datum}T${dva(sat)}:45:00`,
      purpose: 'Test performansi',
    },
    headers,
    'kreiranje rezervacije',
  );

  const id = kreirana.json('data.id');
  if (id) {
    const otkazana = http.post(`${BASE}/schedule/api/bookings/${id}/cancel`, null, {
      headers,
      tags: { naziv: 'otkazivanje rezervacije' },
    });
    check(otkazana, { 'otkazivanje 200': (r) => r.status === 200 });
  }
}
