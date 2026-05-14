const BASE = 'http://localhost:3000/api';

let token = null;
let passed = 0;
let failed = 0;

function printDivider() {
  console.log('------------------------------------------------------------');
}

function printRequest(method, path, headers, body) {
  printDivider();

  console.log('REQUEST');
  console.log('METHOD:', method);
  console.log('URL:', `${BASE}${path}`);

  console.log('HEADERS:');
  console.log(JSON.stringify(headers, null, 2));

  if (body) {
    console.log('BODY:');
    console.log(JSON.stringify(body, null, 2));
  } else {
    console.log('BODY: null');
  }
}

function printResponse(status, data) {
  console.log('RESPONSE');
  console.log('STATUS:', status);

  console.log('BODY:');

  if (data) {
    console.log(JSON.stringify(data, null, 2));
  } else {
    console.log('null');
  }

  printDivider();
  console.log('');
}

async function api(method, path, body, auth = true) {
  const headers = {
    'Content-Type': 'application/json',
  };

  if (auth && token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  printRequest(method, path, headers, body);

  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const data =
    res.status !== 204
      ? await res.json().catch(() => null)
      : null;

  printResponse(res.status, data);

  return {
    status: res.status,
    data,
  };
}

function ok(label, status, expected) {
  const pass = status === expected;

  if (pass) {
    console.log(`PASS: ${label} [${status}]`);
    passed++;
  } else {
    console.log(`FAIL: ${label}`);
    console.log(`EXPECTED: ${expected}`);
    console.log(`RECEIVED: ${status}`);
    failed++;
  }

  console.log('');

  return pass;
}

async function run() {
  let res;

  console.log('AUTH TESTS');
  console.log('');

  res = await api('GET', '/faculties', null, false);
  ok('GET /faculties without token', res.status, 401);

  res = await api(
    'POST',
    '/auth/login',
    {
      enrollment: '367886',
      password: 'wrong',
    },
    false
  );

  ok('POST /auth/login invalid password', res.status, 401);

  res = await api(
    'POST',
    '/auth/login',
    {
      enrollment: '374357',
      password: 'password123',
    },
    false
  );

  if (res.status === 200 && res.data?.token) {
    token = res.data.token;

    console.log('TOKEN SAVED');
    console.log(token);
    console.log('');

    passed++;
  } else {
    console.log('LOGIN FAILED');
    failed++;

    printSummary();
    return;
  }

  console.log('FACULTY TESTS');
  console.log('');

  res = await api('GET', '/faculties');
  ok('GET /faculties', res.status, 200);

  res = await api('POST', '/faculties', {
    name: 'Test Faculty',
  });

  ok('POST /faculties', res.status, 200);

  const facultyId = res.data?.id;

  res = await api('GET', `/faculties/${facultyId}`);
  ok(`GET /faculties/${facultyId}`, res.status, 200);

  res = await api('PUT', `/faculties/${facultyId}`, {
    name: 'Test Faculty Updated',
  });

  ok(`PUT /faculties/${facultyId}`, res.status, 200);

  res = await api('DELETE', `/faculties/${facultyId}`);
  ok(`DELETE /faculties/${facultyId}`, res.status, 204);

  res = await api('GET', `/faculties/${facultyId}`);
  ok(`GET /faculties/${facultyId} after delete`, res.status, 404);

  console.log('BUILDING TESTS');
  console.log('');

  res = await api('GET', '/buildings');
  ok('GET /buildings', res.status, 200);

  res = await api('POST', '/buildings', {
    name: 'Test Building',
    location: 'Campus 9',
  });

  ok('POST /buildings', res.status, 200);

  const buildingId = res.data?.id;

  res = await api('GET', `/buildings/${buildingId}`);
  ok(`GET /buildings/${buildingId}`, res.status, 200);

  res = await api('PUT', `/buildings/${buildingId}`, {
    location: 'Campus 10',
  });

  ok(`PUT /buildings/${buildingId}`, res.status, 200);

  console.log('CUBICLE TESTS');
  console.log('');

  res = await api('GET', '/cubicles');
  ok('GET /cubicles', res.status, 200);

  res = await api('POST', '/cubicles', {
    buildingId,
    identifier: 'TEST-01',
    capacity: 8,
    status: 'AVAILABLE',
  });

  ok('POST /cubicles', res.status, 200);

  const cubicleId = res.data?.id;

  res = await api('GET', `/cubicles/${cubicleId}`);
  ok(`GET /cubicles/${cubicleId}`, res.status, 200);

  res = await api('PUT', `/cubicles/${cubicleId}`, {
    status: 'MAINTENANCE',
  });

  ok(`PUT /cubicles/${cubicleId}`, res.status, 200);

  console.log('USER TESTS');
  console.log('');

  res = await api('GET', '/users');
  ok('GET /users', res.status, 200);

  res = await api('POST', '/users', {
    facultyId: 1,
    enrollment: 'TST9999',
    fullName: 'Test User',
    email: 'testuser@sgrc.mx',
    role: 'STUDENT',
    password: 'test1234',
  });

  ok('POST /users', res.status, 200);

  const userId = res.data?.id;

  res = await api('GET', `/users/${userId}`);
  ok(`GET /users/${userId}`, res.status, 200);

  res = await api('PUT', `/users/${userId}`, {
    fullName: 'Test User Updated',
  });

  ok(`PUT /users/${userId}`, res.status, 200);

  res = await api(
    'POST',
    '/auth/login',
    {
      enrollment: 'TST9999',
      password: 'test1234',
    },
    false
  );

  ok('Login with created user', res.status, 200);

  console.log('RESERVATION TESTS');
  console.log('');

  res = await api('GET', '/reservations');
  ok('GET /reservations', res.status, 200);

  res = await api('POST', '/reservations', {
    userId,
    cubicleId,
    date: '2025-06-15',
    startTime: '09:00:00',
    endTime: '11:00:00',
    status: 'PENDING',
  });

  ok('POST /reservations', res.status, 200);

  const reservationId = res.data?.id;

  res = await api('GET', `/reservations/${reservationId}`);
  ok(`GET /reservations/${reservationId}`, res.status, 200);

  res = await api('PUT', `/reservations/${reservationId}`, {
    status: 'COMPLETED',
  });

  ok(`PUT /reservations/${reservationId}`, res.status, 200);

  res = await api('DELETE', `/reservations/${reservationId}`);
  ok(`DELETE /reservations/${reservationId}`, res.status, 204);

  console.log('CLEANUP');
  console.log('');

  await api('DELETE', `/cubicles/${cubicleId}`);
  await api('DELETE', `/buildings/${buildingId}`);
  await api('DELETE', `/users/${userId}`);

  printSummary();
}

function printSummary() {
  const total = passed + failed;

  console.log('');
  console.log('SUMMARY');
  console.log('PASSED:', passed);
  console.log('FAILED:', failed);
  console.log('TOTAL:', total);
  console.log('');
}

run().catch((err) => {
  console.error('FATAL ERROR');
  console.error(err.message);
  console.error('Is the server running on localhost:8080 ?');
});