import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '1m', target: 3 }, // Ramp up to 3 users
    { duration: '2m', target: 3 }, // Stay at 3 users
    { duration: '1m', target: 10 }, // Ramp up to 10 users
    { duration: '2m', target: 10 }, // Stay at 10 users
    { duration: '1m', target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 95% of requests under 3s
    http_req_failed: ['rate<0.15'], // Error rate under 15%
    errors: ['rate<0.15'],
  },
};

const BASE_URL = 'http://localhost:8081';

// Sample business travel request data
const travelRequests = [
  {
    destination: 'Jakarta',
    startDate: '2024-12-10',
    endDate: '2024-12-12',
    purpose: 'Client meeting - Load test',
    transportationType: 'FLIGHT',
    accommodationType: 'HOTEL',
    estimatedCost: 2500000,
    transportationDetails: 'Garuda Indonesia GA-123',
    accommodationDetails: 'Hotel Indonesia Kempinski',
  },
  {
    destination: 'Surabaya',
    startDate: '2024-11-20',
    endDate: '2024-11-22',
    purpose: 'Site visit - Load test',
    transportationType: 'TRAIN',
    accommodationType: 'HOTEL',
    estimatedCost: 1500000,
    transportationDetails: 'Executive Class Train',
    accommodationDetails: 'Majapahit Hotel',
  },
  {
    destination: 'Bandung',
    startDate: '2024-11-15',
    endDate: '2024-11-16',
    purpose: 'Team building - Load test',
    transportationType: 'CAR',
    accommodationType: 'HOTEL',
    estimatedCost: 800000,
    transportationDetails: 'Company Car',
    accommodationDetails: 'Hilton Bandung',
  },
  {
    destination: 'Bali',
    startDate: '2024-12-20',
    endDate: '2024-12-23',
    purpose: 'Conference - Load test',
    transportationType: 'FLIGHT',
    accommodationType: 'HOTEL',
    estimatedCost: 5000000,
    transportationDetails: 'Lion Air JT-456',
    accommodationDetails: 'Grand Hyatt Bali',
  },
];

export default function () {
  const employeeToken = 'dummy-employee-token';
  const supervisorToken = 'dummy-supervisor-token';

  const employeeHeaders = {
    headers: {
      'Authorization': `Bearer ${employeeToken}`,
      'Content-Type': 'application/json',
    },
  };

  const supervisorHeaders = {
    headers: {
      'Authorization': `Bearer ${supervisorToken}`,
      'Content-Type': 'application/json',
    },
  };

  // Test business travel request submission
  const travelRequest = travelRequests[Math.floor(Math.random() * travelRequests.length)];
  const travelPayload = JSON.stringify(travelRequest);

  const travelResponse = http.post(`${BASE_URL}/api/business-travel/request`, travelPayload, employeeHeaders);

  check(travelResponse, {
    'travel request status is 200 or 400': (r) => r.status === 200 || r.status === 400,
    'travel request response time < 3000ms': (r) => r.timings.duration < 3000,
  }) || errorRate.add(1);

  sleep(1);

  // Test get my travel requests
  const myTravelRequestsResponse = http.get(`${BASE_URL}/api/business-travel/my-requests`, employeeHeaders);

  check(myTravelRequestsResponse, {
    'my travel requests status is 200': (r) => r.status === 200,
    'my travel requests response time < 1500ms': (r) => r.timings.duration < 1500,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test supervisor approval
  const approveTravelResponse = http.post(`${BASE_URL}/api/business-travel/supervisor/approve/1`, null, supervisorHeaders);

  check(approveTravelResponse, {
    'approve travel status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'approve travel response time < 1500ms': (r) => r.timings.duration < 1500,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test supervisor rejection
  const rejectTravelResponse = http.post(`${BASE_URL}/api/business-travel/supervisor/reject/2`, null, supervisorHeaders);

  check(rejectTravelResponse, {
    'reject travel status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'reject travel response time < 1500ms': (r) => r.timings.duration < 1500,
  }) || errorRate.add(1);

  sleep(1);
}