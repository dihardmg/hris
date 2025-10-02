import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '1m', target: 10 }, // Ramp up to 10 users
    { duration: '2m', target: 10 }, // Stay at 10 users
    { duration: '1m', target: 30 }, // Ramp up to 30 users
    { duration: '2m', target: 30 }, // Stay at 30 users
    { duration: '1m', target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 95% of requests under 1s
    http_req_failed: ['rate<0.1'], // Error rate under 10%
    errors: ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8081';

// Simulate different GPS locations around office
const locations = [
  { latitude: -6.2088, longitude: 106.8456 }, // Exact office location
  { latitude: -6.2090, longitude: 106.8458 }, // Nearby location
  { latitude: -6.2086, longitude: 106.8454 }, // Nearby location
  { latitude: -6.2085, longitude: 106.8457 }, // Nearby location
];

// Simulate face image data (base64)
const faceImages = [
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z/C/HgAGgwJ/lK3Q6wAAAABJRU5ErkJggg==',
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwFwAIwgJlU0yKQAAAABJRU5ErkJggg==',
];

export default function () {
  // Simulate user authentication (in real scenario, this would be actual login)
  const token = 'dummy-jwt-token';
  const authHeaders = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };

  // Test clock-in endpoint
  const location = locations[Math.floor(Math.random() * locations.length)];
  const faceImage = faceImages[Math.floor(Math.random() * faceImages.length)];

  const clockInPayload = JSON.stringify({
    latitude: location.latitude,
    longitude: location.longitude,
    faceImage: faceImage,
  });

  const clockInResponse = http.post(`${BASE_URL}/api/attendance/clock-in`, clockInPayload, authHeaders);

  check(clockInResponse, {
    'clock-in status is 200 or 400': (r) => r.status === 200 || r.status === 400, // 400 if already clocked in
    'clock-in response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(1);

  // Test get today's attendance
  const todayResponse = http.get(`${BASE_URL}/api/attendance/today`, authHeaders);

  check(todayResponse, {
    'today attendance status is 200': (r) => r.status === 200,
    'today attendance response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test attendance status
  const statusResponse = http.get(`${BASE_URL}/api/attendance/status`, authHeaders);

  check(statusResponse, {
    'attendance status is 200': (r) => r.status === 200,
    'attendance status response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test clock-out endpoint
  const clockOutResponse = http.post(`${BASE_URL}/api/attendance/clock-out`, null, authHeaders);

  check(clockOutResponse, {
    'clock-out status is 200 or 400': (r) => r.status === 200 || r.status === 400, // 400 if not clocked in
    'clock-out response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(1);

  // Test attendance history
  const historyResponse = http.get(`${BASE_URL}/api/attendance/history?limit=10`, authHeaders);

  check(historyResponse, {
    'attendance history status is 200': (r) => r.status === 200,
    'attendance history response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(1);
}