import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
export let errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    { duration: '2m', target: 10 }, // Ramp up to 10 users
    { duration: '5m', target: 10 }, // Stay at 10 users
    { duration: '2m', target: 50 }, // Ramp up to 50 users
    { duration: '5m', target: 50 }, // Stay at 50 users
    { duration: '2m', target: 100 }, // Ramp up to 100 users
    { duration: '5m', target: 100 }, // Stay at 100 users
    { duration: '2m', target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests under 2s
    http_req_failed: ['rate<0.1'], // Error rate under 10%
    errors: ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8081';

// Test data
const users = [
  { email: 'admin@hris.com', password: 'admin123', role: 'ADMIN' },
  { email: 'john.doe@company.com', password: 'password123', role: 'EMPLOYEE' },
  { email: 'jane.smith@company.com', password: 'password123', role: 'SUPERVISOR' },
];

export function setup() {
  // Setup code if needed
  console.log('Starting k6 load tests for HRIS API');
}

export default function () {
  // Pick a random user
  const user = users[Math.floor(Math.random() * users.length)];

  // Test authentication endpoints
  testAuthentication(user);

  // Test other endpoints based on user role
  if (user.role === 'ADMIN') {
    testAdminEndpoints();
  } else if (user.role === 'EMPLOYEE') {
    testEmployeeEndpoints();
  } else if (user.role === 'SUPERVISOR') {
    testSupervisorEndpoints();
  }

  sleep(1);
}

function testAuthentication(user) {
  // Test login endpoint
  const loginPayload = JSON.stringify({
    email: user.email,
    password: user.password,
  });

  const loginParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const loginResponse = http.post(`${BASE_URL}/api/auth/login`, loginPayload, loginParams);

  check(loginResponse, {
    'login status is 200': (r) => r.status === 200,
    'login response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  if (loginResponse.status === 200) {
    const token = JSON.parse(loginResponse.body).token;

    // Test token validation
    const authHeaders = {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    };

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
    }) || errorRate.add(1);
  }
}

function testEmployeeEndpoints() {
  // These would need actual tokens from authentication
  // For load testing purposes, we'll simulate the requests
  const token = 'dummy-token'; // In real tests, this would come from login

  const authHeaders = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  };

  // Test attendance endpoints
  const attendancePayload = JSON.stringify({
    latitude: -6.2088,
    longitude: 106.8456,
    faceImage: 'base64-encoded-face-image-data',
  });

  const clockInResponse = http.post(`${BASE_URL}/api/attendance/clock-in`, attendancePayload, authHeaders);
  check(clockInResponse, {
    'clock-in status is 200 or 400': (r) => r.status === 200 || r.status === 400, // 400 if already clocked in
    'clock-in response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  const todayAttendanceResponse = http.get(`${BASE_URL}/api/attendance/today`, authHeaders);
  check(todayAttendanceResponse, {
    'today attendance status is 200': (r) => r.status === 200,
    'today attendance response time < 500ms': (r) => r.timings.duration < 500,
  }) || errorRate.add(1);

  // Test leave request
  const leavePayload = JSON.stringify({
    leaveType: 'ANNUAL',
    startDate: '2024-12-01',
    endDate: '2024-12-05',
    reason: 'Load test leave request',
  });

  const leaveResponse = http.post(`${BASE_URL}/api/leave/request`, leavePayload, authHeaders);
  check(leaveResponse, {
    'leave request status is 200 or 400': (r) => r.status === 200 || r.status === 400,
    'leave request response time < 2000ms': (r) => r.timings.duration < 2000,
  }) || errorRate.add(1);
}

function testSupervisorEndpoints() {
  const token = 'dummy-supervisor-token';

  const authHeaders = {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  };

  // Test supervisor approval endpoints
  const approveLeaveResponse = http.post(`${BASE_URL}/api/leave/supervisor/approve/1`, null, authHeaders);
  check(approveLeaveResponse, {
    'approve leave status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'approve leave response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);

  const approveTravelResponse = http.post(`${BASE_URL}/api/business-travel/supervisor/approve/1`, null, authHeaders);
  check(approveTravelResponse, {
    'approve travel status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'approve travel response time < 1000ms': (r) => r.timings.duration < 1000,
  }) || errorRate.add(1);
}

function testAdminEndpoints() {
  const token = 'dummy-admin-token';

  const authHeaders = {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  };

  // Test admin endpoints
  const employeesResponse = http.get(`${BASE_URL}/api/admin/employees`, authHeaders);
  check(employeesResponse, {
    'get employees status is 200': (r) => r.status === 200,
    'get employees response time < 2000ms': (r) => r.timings.duration < 2000,
  }) || errorRate.add(1);

  // Test employee registration
  const employeePayload = JSON.stringify({
    firstName: 'Test',
    lastName: 'User',
    email: `test${Date.now()}@company.com`,
    phoneNumber: '+1234567890',
    departmentId: 1,
    positionId: 1,
    supervisorId: 1,
    hireDate: '2024-01-15',
    annualLeaveBalance: 12,
    sickLeaveBalance: 10,
    password: 'tempPassword123',
  });

  const registerResponse = http.post(`${BASE_URL}/api/admin/register-employee`, employeePayload, authHeaders);
  check(registerResponse, {
    'register employee status is 200 or 400': (r) => r.status === 200 || r.status === 400,
    'register employee response time < 3000ms': (r) => r.timings.duration < 3000,
  }) || errorRate.add(1);
}

export function teardown() {
  console.log('Load tests completed');
}