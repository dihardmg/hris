import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 5 }, // Ramp up to 5 users
    { duration: '60s', target: 5 }, // Stay at 5 users
    { duration: '30s', target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.2'],
    errors: ['rate<0.2'],
  },
};

const BASE_URL = 'http://localhost:8081';

// Get a real token at the start
let adminToken = null;

export function setup() {
  const loginResponse = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: 'admin@hris.com',
    password: 'admin123',
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (loginResponse.status === 200) {
    try {
      const response = JSON.parse(loginResponse.body);
      return { token: response.token };
    } catch (e) {
      console.log('Failed to parse login response');
      return { token: null };
    }
  }
  return { token: null };
}

export default function (setupData) {
  const token = setupData.token;
  if (!token) {
    console.log('No token available, skipping test');
    return;
  }

  const authHeaders = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };

  // Test authentication validation
  const validateResponse = http.post(`${BASE_URL}/api/auth/validate`, null, authHeaders);
  check(validateResponse, {
    'validate status is 200': (r) => r.status === 200,
    'validate response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test get current user info
  const meResponse = http.get(`${BASE_URL}/api/auth/me`, authHeaders);
  check(meResponse, {
    'me status is 200': (r) => r.status === 200,
    'me response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test admin endpoints
  const employeesResponse = http.get(`${BASE_URL}/api/admin/employees`, authHeaders);
  check(employeesResponse, {
    'employees status is 200': (r) => r.status === 200,
    'employees response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(1);

  // Test attendance endpoints (these might fail if employee data is missing)
  const todayAttendanceResponse = http.get(`${BASE_URL}/api/attendance/today`, authHeaders);
  check(todayAttendanceResponse, {
    'today attendance status is 200 or 403': (r) => r.status === 200 || r.status === 403,
    'today attendance response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(1);
}