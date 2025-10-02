
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// Custom trend metric for response time
const loginResponseTime = new Trend('login_response_time');

export const options = {
  stages: [
    { duration: '10s', target: 10 },
    { duration: '10s', target: 100 },
  ],
  thresholds: {
    'http_req_duration{api:login}': ['p(95)<=200'],
  },
};

export default function () {
  const url = 'http://localhost:8081/api/auth/login';
  const payload = JSON.stringify({
    email: 'admin@hris.com',
    password: 'admin123',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: {
      api: 'login',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  loginResponseTime.add(res.timings.duration);

  sleep(1);
}
