import http from 'k6/http';
import { check, sleep } from 'k6';

// Stress test - find breaking point
export const options = {
  stages: [
    { duration: '30s', target: 50 },   // Ramp up to 50
    { duration: '1m', target: 100 },   // Ramp up to 100
    { duration: '30s', target: 150 },  // Push to 150
    { duration: '1m', target: 0 },     // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.3'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const EMAIL = __ENV.EMAIL || 'mcrdik@gmail.com';
const PASSWORD = __ENV.PASSWORD || 'week123';

export default function () {
  const response = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({
      email: EMAIL,
      password: PASSWORD,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    }
  );

  check(response, {
    'status is 200, 401, or 429': (r) => [200, 401, 429].includes(r.status),
    'response time < 5s': (r) => r.timings.duration < 5000,
  });

  sleep(0.1);
}