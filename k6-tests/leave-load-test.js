import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '1m', target: 5 }, // Ramp up to 5 users
    { duration: '3m', target: 5 }, // Stay at 5 users
    { duration: '1m', target: 15 }, // Ramp up to 15 users
    { duration: '3m', target: 15 }, // Stay at 15 users
    { duration: '1m', target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests under 2s
    http_req_failed: ['rate<0.1'], // Error rate under 10%
    errors: ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8081';

// Sample leave request data
const leaveRequests = [
  {
    leaveType: 'ANNUAL',
    startDate: '2024-12-01',
    endDate: '2024-12-05',
    reason: 'Annual vacation - Load test',
  },
  {
    leaveType: 'SICK',
    startDate: '2024-11-15',
    endDate: '2024-11-16',
    reason: 'Medical appointment - Load test',
  },
  {
    leaveType: 'MATERNITY',
    startDate: '2024-10-01',
    endDate: '2024-12-31',
    reason: 'Maternity leave - Load test',
  },
  {
    leaveType: 'PATERNITY',
    startDate: '2024-11-01',
    endDate: '2024-11-15',
    reason: 'Paternity leave - Load test',
  },
  {
    leaveType: 'PERSONAL',
    startDate: '2024-11-20',
    endDate: '2024-11-20',
    reason: 'Personal matters - Load test',
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

  // Test leave request submission
  const leaveRequest = leaveRequests[Math.floor(Math.random() * leaveRequests.length)];
  const leavePayload = JSON.stringify(leaveRequest);

  const leaveResponse = http.post(`${BASE_URL}/api/leave/request`, leavePayload, employeeHeaders);

  check(leaveResponse, {
    'leave request status is 200 or 400': (r) => r.status === 200 || r.status === 400,
    'leave request response time < 2000ms': (r) => r.timings.duration < 2000,
  }) || errorRate.add(1);

  sleep(1);

  // Test get my leave requests
  const myRequestsResponse = http.get(`${BASE_URL}/api/leave/my-requests`, employeeHeaders);

  check(myRequestsResponse, {
    'my requests status is 200': (r) => r.status === 200,
    'my requests response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test get current leave
  const currentLeaveResponse = http.get(`${BASE_URL}/api/leave/current`, employeeHeaders);

  check(currentLeaveResponse, {
    'current leave status is 200': (r) => r.status === 200,
    'current leave response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(0.5);

  // Test supervisor approval/rejection
  const approveResponse = http.post(`${BASE_URL}/api/leave/supervisor/approve/1`, null, supervisorHeaders);

  check(approveResponse, {
    'approve leave status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'approve leave response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(0.5);

  const rejectResponse = http.post(`${BASE_URL}/api/leave/supervisor/reject/2`, null, supervisorHeaders);

  check(rejectResponse, {
    'reject leave status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'reject leave response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  sleep(1);
}