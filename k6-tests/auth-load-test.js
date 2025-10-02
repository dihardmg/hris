import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '1m', target: 20 }, // Ramp up to 20 users
    { duration: '3m', target: 20 }, // Stay at 20 users
    { duration: '1m', target: 50 }, // Ramp up to 50 users
    { duration: '3m', target: 50 }, // Stay at 50 users
    { duration: '1m', target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests under 500ms
    http_req_failed: ['rate<0.05'], // Error rate under 5%
    errors: ['rate<0.05'],
  },
};

const BASE_URL = 'http://localhost:8081';

const credentials = [
  { email: 'admin@hris.com', password: 'admin123' },
];

export default function () {
  // Test login endpoint with different credentials
  const cred = credentials[Math.floor(Math.random() * credentials.length)];

  const loginPayload = JSON.stringify({
    email: cred.email,
    password: cred.password,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Test login endpoint
  const loginResponse = http.post(`${BASE_URL}/api/auth/login`, loginPayload, params);

  check(loginResponse, {
    'login status is 200': (r) => r.status === 200,
    'login response time < 500ms': (r) => r.timings.duration < 500,
    'login response not empty': (r) => r.body && r.body.length > 0,
  }) || errorRate.add(1);

  if (loginResponse.status === 200) {
    try {
      const token = JSON.parse(loginResponse.body).token;
      const authHeaders = {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      };

      // Test token validation
      const validateResponse = http.post(`${BASE_URL}/api/auth/validate`, null, authHeaders);
      check(validateResponse, {
        'validate status is 200': (r) => r.status === 200,
        'validate response time < 300ms': (r) => r.timings.duration < 300,
      }) || errorRate.add(1);

      // Test get current user info
      const meResponse = http.get(`${BASE_URL}/api/auth/me`, authHeaders);
      check(meResponse, {
        'me status is 200': (r) => r.status === 200,
        'me response time < 300ms': (r) => r.timings.duration < 300,
        'me response not empty': (r) => r.body && r.body.length > 0,
      }) || errorRate.add(1);
    } catch (e) {
      console.log('Failed to parse login response:', loginResponse.body);
      errorRate.add(1);
    }
  }

  sleep(0.5);
}