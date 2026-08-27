// Test izdrzljivosti: postepeno povecanje broja korisnika dok se ne uoci
// pogorsanje, da bi se videla granica do koje sistem radi u zadatim okvirima.
//
// Za razliku od load.js, ovde pragovi NISU uslov prolaza — cilj je izmeriti
// gde performanse pocinju da padaju, pa se rezultat cita iz izvestaja.
//
// Pokretanje (sistem mora biti podignut):
//   docker run --rm --network erp-fakultet_default -v "%cd%/tools/perf:/perf" grafana/k6 run /perf/stress.js
import { sleep } from 'k6';
import { prijava, get } from './lib.js';

export const options = {
  stages: [
    { duration: '20s', target: 20 },
    { duration: '20s', target: 50 },
    { duration: '20s', target: 100 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    // Samo evidentiranje; ne obara test, sluzi za citanje granice.
    http_req_duration: ['p(95)<2000'],
  },
};

export default function () {
  const headers = prijava();
  get('/hr/api/workers?page=0&size=20', headers, 'lista zaposlenih');
  get('/schedule/api/rooms?page=0&size=20', headers, 'lista prostorija');
  sleep(0.5);
}
