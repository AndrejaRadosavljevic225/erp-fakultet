// Zajednicke funkcije za testove performansi (k6).
import http from 'k6/http';
import { check } from 'k6';

export const BASE = __ENV.BASE || 'http://api-gateway:8080';
const JSON_HEADERS = { 'Content-Type': 'application/json' };

/** Prijava; vraca Authorization zaglavlje za dalje pozive. */
export function prijava(korisnik = 'admin', lozinka = 'admin123') {
  const res = http.post(
    `${BASE}/hr/api/auth/login`,
    JSON.stringify({ usernameOrEmail: korisnik, password: lozinka }),
    { headers: JSON_HEADERS, tags: { naziv: 'prijava' } },
  );
  check(res, { 'prijava 200': (r) => r.status === 200 });
  const token = res.json('data.token');
  return { ...JSON_HEADERS, Authorization: `Bearer ${token}` };
}

/** GET uz proveru statusa; `naziv` grupise merenja po endpointu. */
export function get(putanja, headers, naziv) {
  const res = http.get(`${BASE}${putanja}`, { headers, tags: { naziv } });
  check(res, { [`${naziv} 200`]: (r) => r.status === 200 });
  return res;
}

/** POST uz proveru statusa. */
export function post(putanja, telo, headers, naziv, ocekivaniStatus = 200) {
  const res = http.post(`${BASE}${putanja}`, JSON.stringify(telo), { headers, tags: { naziv } });
  check(res, { [`${naziv} ${ocekivaniStatus}`]: (r) => r.status === ocekivaniStatus });
  return res;
}

const dva = (n) => String(n).padStart(2, '0');

/** Datum pomeren za zadati broj dana, u obliku "YYYY-MM-DD". */
function datum(pomerajDana) {
  const dan = new Date();
  dan.setDate(dan.getDate() + pomerajDana);
  return dan.toISOString().slice(0, 10);
}

/** Termin unutar jednog dana (LocalDateTime bez vremenske zone). */
export function interval(pomerajDana, pocetniSat, trajanjeSati = 1) {
  const d = datum(pomerajDana);
  return {
    startDateTime: `${d}T${dva(pocetniSat)}:00:00`,
    endDateTime: `${d}T${dva(pocetniSat + trajanjeSati)}:00:00`,
  };
}

/** Interval preko vise dana, od ponoci do ponoci — za kalendar zauzetosti. */
export function intervalDana(pomerajOd, pomerajDo) {
  return {
    startDateTime: `${datum(pomerajOd)}T00:00:00`,
    endDateTime: `${datum(pomerajDo)}T00:00:00`,
  };
}
