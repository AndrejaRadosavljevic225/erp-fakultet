// Kompletan API test ERP sistema kroz api-gateway.
const BASE = process.env.BASE ?? 'http://localhost:8080';
const S = Date.now().toString().slice(-7); // sufiks za jedinstvene vrednosti

let pass = 0;
const failures = [];
const created = { workers: [], users: [], positions: [], wp: [], roles: [], perms: [], rooms: [], years: [], norms: [], assigns: [] };

function check(name, cond, detail = '') {
  if (cond) {
    pass++;
    console.log(`  PASS  ${name}`);
  } else {
    failures.push(`${name}${detail ? ' -> ' + detail : ''}`);
    console.log(`  FAIL  ${name}${detail ? ' -> ' + detail : ''}`);
  }
}

async function req(method, path, { token, body, raw } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(BASE + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  let json = null;
  try {
    json = await res.json();
  } catch {
    /* prazno telo */
  }
  return raw ? { status: res.status, json } : { status: res.status, json, data: json?.data };
}

const get = (p, t) => req('GET', p, { token: t });
const post = (p, b, t) => req('POST', p, { token: t, body: b });
const put = (p, b, t) => req('PUT', p, { token: t, body: b });
const del = (p, t) => req('DELETE', p, { token: t });

async function login(usernameOrEmail, password) {
  const r = await post('/hr/api/auth/login', { usernameOrEmail, password });
  return r.status === 200 ? r.data.token : null;
}

function group(title) {
  console.log(`\n=== ${title}`);
}

const run = async () => {
  // ---------------------------------------------------------------- AUTH
  group('AUTH i /me');
  const admin = await login('admin', 'admin123');
  check('prijava korisnickim imenom', !!admin);

  const me = await get('/hr/api/auth/me', admin);
  check('GET /auth/me vraca ADMIN rolu', me.status === 200 && me.data.roleCode === 'ADMIN', JSON.stringify(me.data));
  check('/auth/me sadrzi permisije', Array.isArray(me.data?.permissions));

  const noTok = await get('/hr/api/auth/me');
  check('/auth/me bez tokena je odbijen', noTok.status === 401 || noTok.status === 403, `status ${noTok.status}`);

  const badPw = await post('/hr/api/auth/login', { usernameOrEmail: 'admin', password: 'pogresno' });
  check('pogresna lozinka -> 400', badPw.status === 400);

  const badUser = await post('/hr/api/auth/login', { usernameOrEmail: 'nepostojeci' + S, password: 'x' });
  check('nepostojeci korisnik -> 400', badUser.status === 400);

  // ---------------------------------------------------------- PRIPREMA
  group('Priprema test podataka');
  const roles = await get('/hr/api/roles?size=100', admin);
  const roleId = (code) => roles.data.content.find((r) => r.code === code)?.id;
  check('difoltne role ADMIN/HR/PROFESOR postoje', !!roleId('ADMIN') && !!roleId('HR') && !!roleId('PROFESOR'));

  const wProf = await post(
    '/hr/api/workers',
    {
      firstName: 'Test',
      lastName: 'Profesor' + S,
      email: `prof.${S}@fakultet.rs`,
      personalId: '1' + S + '00000',
      hireDate: '2026-01-10',
      employmentStatus: 'ACTIVE',
      employmentType: 'FULL_TIME',
    },
    admin,
  );
  check('kreiran radnik za profesora', wProf.status === 200, JSON.stringify(wProf.json));
  created.workers.push(wProf.data.id);

  const uProf = await post(
    '/hr/api/users',
    { username: 'prof' + S, password: 'test1234', workerId: wProf.data.id, roleId: roleId('PROFESOR'), isActive: true },
    admin,
  );
  check('kreiran nalog profesora', uProf.status === 200);
  created.users.push(uProf.data.id);

  const uHr = await post(
    '/hr/api/users',
    { username: 'hr' + S, password: 'test1234', roleId: roleId('HR'), isActive: true },
    admin,
  );
  check('kreiran HR nalog', uHr.status === 200);
  created.users.push(uHr.data.id);

  const prof = await login('prof' + S, 'test1234');
  const hr = await login('hr' + S, 'test1234');
  check('prijava profesora', !!prof);
  check('prijava HR-a', !!hr);

  const profMe = await get('/hr/api/auth/me', prof);
  check('/auth/me za profesora vraca workerId', profMe.data?.workerId === wProf.data.id);
  check('/auth/me vraca ime zaposlenog', !!profMe.data?.workerFullName);

  // prijava preko email-a zaposlenog
  const byEmail = await login(`prof.${S}@fakultet.rs`, 'test1234');
  check('prijava preko EMAIL-a zaposlenog', !!byEmail);
  const aliasLogin = await post('/hr/api/auth/login', { username: `prof.${S}@fakultet.rs`, password: 'test1234' });
  check('stari kljuc "username" i dalje radi (JsonAlias)', aliasLogin.status === 200);

  // ---------------------------------------------------------------- RBAC
  group('Autorizacija po rolama');
  check('PROFESOR cita listu zaposlenih', (await get('/hr/api/workers?size=5', prof)).status === 200);
  check(
    'PROFESOR ne sme da kreira zaposlenog',
    (await post('/hr/api/workers', { firstName: 'X', lastName: 'Y', email: 'x' + S + '@f.rs', personalId: '9' + S + '00000', hireDate: '2026-01-01' }, prof)).status === 403,
  );
  check('PROFESOR ne vidi korisnicke naloge', (await get('/hr/api/users?size=5', prof)).status === 403);
  check('PROFESOR ne vidi audit log', (await get('/hr/api/audit-logs?size=5', prof)).status === 403);
  check('HR cita listu zaposlenih', (await get('/hr/api/workers?size=5', hr)).status === 200);
  check('HR ne sme da kreira rolu', (await post('/hr/api/roles', { code: 'X' + S, name: 'X' }, hr)).status === 403);
  check('HR cita role', (await get('/hr/api/roles?size=5', hr)).status === 200);

  // ------------------------------------------------------------ HR CRUD
  group('HR: zaposleni');
  const w = await post(
    '/hr/api/workers',
    {
      firstName: 'Ana',
      lastName: 'Anic' + S,
      email: `ana.${S}@fakultet.rs`,
      personalId: '2' + S + '00000',
      hireDate: '2026-02-01',
      employmentStatus: 'ACTIVE',
      employmentType: 'FULL_TIME',
    },
    admin,
  );
  check('kreiranje zaposlenog', w.status === 200);
  created.workers.push(w.data.id);

  const dup = await post(
    '/hr/api/workers',
    { firstName: 'Ana', lastName: 'Duplikat', email: `druga.${S}@fakultet.rs`, personalId: '2' + S + '00000', hireDate: '2026-02-01' },
    admin,
  );
  check('duplikat JMBG -> 400', dup.status === 400, dup.json?.message);

  const invalid = await post('/hr/api/workers', { firstName: '', lastName: '', email: 'nijeemail', personalId: '', hireDate: null }, admin);
  check('validacija praznih polja -> 400 sa listom gresaka', invalid.status === 400 && Array.isArray(invalid.json?.validationErrors) && invalid.json.validationErrors.length > 0);

  const detail = await get(`/hr/api/workers/${w.data.id}`, admin);
  check('detalj zaposlenog ima listu pozicija', detail.status === 200 && Array.isArray(detail.data.positions));

  const upd = await put(`/hr/api/workers/${w.data.id}`, { phone: '0601112223', employmentStatus: 'ON_LEAVE' }, admin);
  check('izmena zaposlenog (status ON_LEAVE)', upd.status === 200 && upd.data.employmentStatus === 'ON_LEAVE');

  const byStatus = await get('/hr/api/workers/status/ON_LEAVE', admin);
  check('filtriranje po statusu', byStatus.status === 200 && byStatus.data.some((x) => x.id === w.data.id));

  const search = await get(`/hr/api/workers?searchTerm=Anic${S}`, admin);
  check('pretraga po prezimenu', search.status === 200 && search.data.totalElements >= 1);

  const notFound = await get('/hr/api/workers/99999999', admin);
  check('nepostojeci zaposleni -> 404', notFound.status === 404);

  group('HR: radna mesta i dodele');
  const pos = await post('/hr/api/positions', { title: 'Docent ' + S, salaryGrade: 'A2', baseSalary: 120000, isVacant: true }, admin);
  check('kreiranje radnog mesta', pos.status === 200);
  created.positions.push(pos.data.id);

  const posUpd = await put(`/hr/api/positions/${pos.data.id}`, { baseSalary: 130000 }, admin);
  check('izmena radnog mesta', posUpd.status === 200 && Number(posUpd.data.baseSalary) === 130000);

  const wp = await post('/hr/api/worker-positions', { workerId: w.data.id, positionId: pos.data.id, validFrom: '2026-02-01', fraction: 1, isPrimary: true }, admin);
  check('dodela radnog mesta', wp.status === 200);
  created.wp.push(wp.data.id);

  const wpList = await get(`/hr/api/worker-positions/worker/${w.data.id}`, admin);
  check('lista dodela za zaposlenog', wpList.status === 200 && wpList.data.length === 1);

  const wpUpd = await put(`/hr/api/worker-positions/${wp.data.id}`, { fraction: 0.5 }, admin);
  check('izmena dodele', wpUpd.status === 200 && Number(wpUpd.data.fraction) === 0.5);

  const detail2 = await get(`/hr/api/workers/${w.data.id}`, admin);
  check('dodela se vidi u detalju zaposlenog', detail2.data.positions.length === 1 && detail2.data.positions[0].positionTitle === 'Docent ' + S);

  group('Administracija: nalozi, role, permisije, audit');
  const usr = await post('/hr/api/users', { username: 'kor' + S, password: 'test1234', workerId: w.data.id, roleId: roleId('PROFESOR'), isActive: true }, admin);
  check('kreiranje naloga', usr.status === 200);
  created.users.push(usr.data.id);

  const usrDup = await post('/hr/api/users', { username: 'kor' + S, password: 'test1234' }, admin);
  check('duplo korisnicko ime -> 400', usrDup.status === 400);

  const usrUpd = await put(`/hr/api/users/${usr.data.id}`, { isActive: false }, admin);
  check('deaktivacija naloga', usrUpd.status === 200 && usrUpd.data.active === false);
  const deactivated = await post('/hr/api/auth/login', { usernameOrEmail: 'kor' + S, password: 'test1234' });
  check('deaktiviran nalog ne moze da se prijavi', deactivated.status === 400);

  const role = await post('/hr/api/roles', { code: 'TEST' + S, name: 'Test rola', description: 'za test' }, admin);
  check('kreiranje role', role.status === 200);
  created.roles.push(role.data.id);

  const perm = await post('/hr/api/permissions', { code: 'TEST_PERM' + S, name: 'Test permisija', module: 'HR' }, admin);
  check('kreiranje permisije', perm.status === 200);
  created.perms.push(perm.data.id);

  const assignPerm = await post(`/hr/api/roles/${role.data.id}/permissions/${perm.data.id}`, undefined, admin);
  check('dodela permisije roli', assignPerm.status === 200);
  const rolePerms = await get(`/hr/api/roles/${role.data.id}/permissions`, admin);
  check('lista permisija role', rolePerms.status === 200 && rolePerms.data.length === 1);
  const removePerm = await del(`/hr/api/roles/${role.data.id}/permissions/${perm.data.id}`, admin);
  check('uklanjanje permisije sa role', removePerm.status === 200);
  check('permisija je uklonjena', (await get(`/hr/api/roles/${role.data.id}/permissions`, admin)).data.length === 0);

  const audit = await get('/hr/api/audit-logs?size=10', admin);
  check('audit log lista', audit.status === 200 && audit.data.totalElements > 0);
  const auditW = await get(`/hr/api/audit-logs/entity/Worker/${w.data.id}`, admin);
  check('audit po entitetu belezi CREATE i UPDATE', auditW.status === 200 && auditW.data.length >= 2, `zapisa: ${auditW.data?.length}`);

  // ------------------------------------------------------------ SCHEDULE
  group('Raspored: prostorije');
  const room = await post('/schedule/api/rooms', { code: 'T' + S, name: 'Test sala', building: 'Zgrada' + S, capacity: 50, bookable: true }, admin);
  check('kreiranje prostorije', room.status === 200);
  created.rooms.push(room.data.id);

  const room2 = await post('/schedule/api/rooms', { code: 'T2' + S, name: 'Mala sala', building: 'Zgrada' + S, capacity: 10, bookable: true }, admin);
  check('kreiranje druge prostorije', room2.status === 200);
  created.rooms.push(room2.data.id);

  const roomUpd = await put(`/schedule/api/rooms/${room.data.id}`, { capacity: 80 }, admin);
  check('izmena prostorije', roomUpd.status === 200 && roomUpd.data.capacity === 80);
  check('PROFESOR cita prostorije', (await get('/schedule/api/rooms?size=5', prof)).status === 200);
  check('PROFESOR ne sme da kreira prostoriju', (await post('/schedule/api/rooms', { code: 'X' + S }, prof)).status === 403);

  group('Raspored: skolske godine i rezervacije');
  const year = await post('/schedule/api/school-years', { code: `${S}/27`, startDate: '2026-10-01', endDate: '2027-09-30' }, admin);
  check('kreiranje skolske godine', year.status === 200);
  created.years.push(year.data.id);

  const avail1 = await post('/schedule/api/bookings/availability', { roomId: room.data.id, startDateTime: '2027-03-01T10:00:00', endDateTime: '2027-03-01T12:00:00' }, admin);
  check('provera dostupnosti: slobodno', avail1.status === 200 && avail1.data.available === true);

  const bk = await post(
    '/schedule/api/bookings',
    { roomId: room.data.id, requesterWorkerId: wProf.data.id, schoolYearId: year.data.id, startDateTime: '2027-03-01T10:00:00', endDateTime: '2027-03-01T12:00:00', purpose: 'Predavanje', teachingType: 'REGULAR' },
    admin,
  );
  check('kreiranje rezervacije (status REQUESTED)', bk.status === 200 && bk.data.status === 'REQUESTED');
  check('trajanje se racuna (2 h)', bk.data?.durationHours === 2);

  const avail2 = await post('/schedule/api/bookings/availability', { roomId: room.data.id, startDateTime: '2027-03-01T11:00:00', endDateTime: '2027-03-01T13:00:00' }, admin);
  check('provera dostupnosti: zauzeto uz listu konflikata', avail2.data?.available === false && avail2.data.conflicts.length === 1);

  const overlap = await post('/schedule/api/bookings', { roomId: room.data.id, requesterWorkerId: wProf.data.id, startDateTime: '2027-03-01T11:00:00', endDateTime: '2027-03-01T13:00:00' }, admin);
  check('preklapanje se odbija -> 400', overlap.status === 400, overlap.json?.message);

  const badRange = await post('/schedule/api/bookings', { roomId: room.data.id, requesterWorkerId: wProf.data.id, startDateTime: '2027-03-02T12:00:00', endDateTime: '2027-03-02T10:00:00' }, admin);
  check('kraj pre pocetka -> 400', badRange.status === 400);

  const profBooking = await post(
    '/schedule/api/bookings',
    { roomId: room.data.id, requesterWorkerId: 1, startDateTime: '2027-03-03T10:00:00', endDateTime: '2027-03-03T11:00:00', purpose: 'Profesorova' },
    prof,
  );
  check('PROFESOR-u se forsira sopstveni workerId', profBooking.status === 200 && profBooking.data.requesterWorkerId === wProf.data.id, `dobijeno ${profBooking.data?.requesterWorkerId}`);
  check('PROFESOR ne sme da odobri rezervaciju', (await post(`/schedule/api/bookings/${bk.data.id}/approve`, undefined, prof)).status === 403);

  const approved = await post(`/schedule/api/bookings/${bk.data.id}/approve?approvedBy=1`, undefined, admin);
  check('odobravanje rezervacije', approved.status === 200 && approved.data.status === 'ACCEPTED');

  const rejected = await post(`/schedule/api/bookings/${profBooking.data.id}/reject?approvedBy=1`, undefined, admin);
  check('odbijanje rezervacije', rejected.status === 200 && rejected.data.status === 'REJECTED');

  const bk2 = await post('/schedule/api/bookings', { roomId: room2.data.id, requesterWorkerId: wProf.data.id, startDateTime: '2027-03-04T08:00:00', endDateTime: '2027-03-04T09:00:00' }, admin);
  const cancelled = await post(`/schedule/api/bookings/${bk2.data.id}/cancel`, undefined, admin);
  check('otkazivanje rezervacije', cancelled.status === 200 && cancelled.data.status === 'CANCELLED');

  const byStatusB = await get('/schedule/api/bookings/status/REQUESTED?size=5', admin);
  check('lista po statusu REQUESTED', byStatusB.status === 200);
  const byWorker = await get(`/schedule/api/bookings/worker/${wProf.data.id}?size=10`, admin);
  check('lista rezervacija po zaposlenom', byWorker.status === 200 && byWorker.data.totalElements >= 2);

  group('Raspored: zauzetost (UC-SC-05)');
  const occInside = await get(`/schedule/api/bookings/room/${room.data.id}/occupancy?from=2027-03-01T11:00:00&to=2027-03-01T11:30:00`, admin);
  check('zauzetost hvata termin koji je poceo pre "from" (overlap)', occInside.status === 200 && occInside.data.length === 1);

  const occAll = await get('/schedule/api/bookings/occupancy?from=2027-03-01T00:00:00&to=2027-03-05T00:00:00', admin);
  check('zauzetost svih sala', occAll.status === 200 && occAll.data.length >= 1);
  check('odbijene i otkazane se ne prikazuju', !occAll.data.some((b) => b.status === 'REJECTED' || b.status === 'CANCELLED'));

  const occBuilding = await get(`/schedule/api/bookings/occupancy?from=2027-03-01T00:00:00&to=2027-03-05T00:00:00&building=Zgrada${S}`, admin);
  check('filter po zgradi', occBuilding.status === 200 && occBuilding.data.length >= 1);
  const occBuildingNone = await get('/schedule/api/bookings/occupancy?from=2027-03-01T00:00:00&to=2027-03-05T00:00:00&building=NePostoji', admin);
  check('filter po nepostojecoj zgradi vraca prazno', occBuildingNone.data?.length === 0);
  const occCap = await get('/schedule/api/bookings/occupancy?from=2027-03-01T00:00:00&to=2027-03-05T00:00:00&minCapacity=5000', admin);
  check('filter po minimalnom kapacitetu', occCap.data?.length === 0);

  group('Nastava: norme, dodele i fond casova');
  const norm = await post('/schedule/api/teaching/norms', { roleId: roleId('PROFESOR'), schoolYearId: year.data.id, requiredHours: 10, description: 'test norma' }, admin);
  check('kreiranje norme', norm.status === 200);
  created.norms.push(norm.data.id);

  const assign = await post('/schedule/api/teaching/assignments', { schoolYearId: year.data.id, workerId: wProf.data.id, roleId: roleId('PROFESOR'), normId: norm.data.id }, admin);
  check('dodela nastavnika skolskoj godini', assign.status === 200);
  created.assigns.push(assign.data.id);

  const assigns = await get(`/schedule/api/teaching/assignments/year/${year.data.id}`, admin);
  check('lista dodela za godinu', assigns.status === 200 && assigns.data.length === 1);

  const report = await get(`/schedule/api/teaching/report?workerId=${wProf.data.id}&schoolYearId=${year.data.id}`, admin);
  check('fond casova: norma je 10 h', report.status === 200 && report.data.requiredHours === 10);
  check('fond casova: realizovano 2 h iz odobrenog termina', report.data?.realizedHours === 2, `dobijeno ${report.data?.realizedHours}`);
  check('fond casova: odstupanje -8 h', report.data?.deviation === -8, `dobijeno ${report.data?.deviation}`);
  check('fond casova: norma nije ispunjena', report.data?.fulfilled === false);

  const reportYear = await get(`/schedule/api/teaching/report/year/${year.data.id}`, admin);
  check('izvestaj za celu godinu', reportYear.status === 200 && reportYear.data.length === 1);

  const ownReport = await get(`/schedule/api/teaching/report?workerId=${wProf.data.id}&schoolYearId=${year.data.id}`, prof);
  check('PROFESOR vidi svoj fond casova', ownReport.status === 200);
  const otherReport = await get(`/schedule/api/teaching/report?workerId=1&schoolYearId=${year.data.id}`, prof);
  check('PROFESOR ne vidi tudji fond casova', otherReport.status === 403, `status ${otherReport.status}`);

  // ------------------------------------------------------------- CISCENJE
  group('Ciscenje test podataka');
  for (const id of created.assigns) await del(`/schedule/api/teaching/assignments/${id}`, admin);
  for (const id of created.norms) await del(`/schedule/api/teaching/norms/${id}`, admin);
  const bookingIds = [bk.data.id, profBooking.data?.id, bk2.data?.id].filter(Boolean);
  for (const id of bookingIds) await post(`/schedule/api/bookings/${id}/cancel`, undefined, admin);
  for (const id of created.rooms) await del(`/schedule/api/rooms/${id}`, admin);
  for (const id of created.years) await del(`/schedule/api/school-years/${id}`, admin);
  for (const id of created.wp) await del(`/hr/api/worker-positions/${id}`, admin);
  for (const id of created.users) await del(`/hr/api/users/${id}`, admin);
  for (const id of created.perms) await del(`/hr/api/permissions/${id}`, admin);
  for (const id of created.roles) await del(`/hr/api/roles/${id}`, admin);
  for (const id of created.positions) await del(`/hr/api/positions/${id}`, admin);
  for (const id of created.workers) await del(`/hr/api/workers/${id}`, admin);
  const gone = await get(`/hr/api/workers/${w.data.id}`, admin);
  check('obrisan zaposleni vise ne postoji', gone.status === 404, `status ${gone.status}`);

  console.log(`\n================ REZULTAT ================`);
  console.log(`PROSLO: ${pass}   PALO: ${failures.length}`);
  if (failures.length) {
    console.log('\nNeuspesni testovi:');
    failures.forEach((f) => console.log('  - ' + f));
    process.exitCode = 1;
  }
};

run().catch((e) => {
  console.error('GRESKA U SKRIPTI:', e);
  process.exitCode = 2;
});
