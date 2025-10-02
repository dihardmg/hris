
import http from 'k6/http';
import { check, group, sleep } from 'k6';

export const options = {
  scenarios: {
    low_load: {
      executor: 'constant-vus',
      vus: 10,
      duration: '10s',
      tags: { test_type: 'low_load' },
    },
    high_load: {
      executor: 'constant-vus',
      vus: 100,
      duration: '10s',
      startTime: '15s',
      tags: { test_type: 'high_load' },
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<=200'],
  },
};

const BASE_URL = 'http://localhost:8081/api';

// Helper function to generate random data
function randomString(length) {
  const charset = 'abcdefghijklmnopqrstuvwxyz';
  let res = '';
  for (let i = 0; i < length; i++) {
    res += charset.charAt(Math.floor(Math.random() * charset.length));
  }
  return res;
}

export function setup() {
  // Login as admin to get a token for other requests
  const loginPayload = JSON.stringify({
    email: 'admin@hris.com',
    password: 'admin123',
  });
  const params = {
    headers: { 'Content-Type': 'application/json' },
  };
  const res = http.post(`${BASE_URL}/auth/login`, loginPayload, params);
  check(res, { 'admin login successful': (r) => r.status === 200 });
  const token = res.json('token');
  return { authToken: token };
}

export default function (data) {
  if (!data.authToken) {
    console.log("Authentication failed in setup, skipping iteration.");
    return;
  }

  const authToken = data.authToken;
  const authParams = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`,
    },
  };

  group('Authentication', function () {
    const validatePayload = JSON.stringify({ token: authToken });
    const resVal = http.post(`${BASE_URL}/auth/validate`, validatePayload, authParams);
    check(resVal, { 'token validation': (r) => r.status === 200 });

    const resMe = http.get(`${BASE_URL}/auth/me`, authParams);
    check(resMe, { 'get current user': (r) => r.status === 200 });
  });

  group('Attendance', function () {
    const clockInPayload = JSON.stringify({
        latitude: -6.2088,
        longitude: 106.8456,
        faceTemplate: "mock-face-template-string"
    });
    const resClockIn = http.post(`${BASE_URL}/attendance/clock-in`, clockInPayload, authParams);
    check(resClockIn, { 'clock-in': (r) => r.status === 200 || r.status === 400 }); // 400 if already clocked in

    sleep(1); // wait 1 second before clocking out

    const resClockOut = http.post(`${BASE_URL}/attendance/clock-out`, '{}', authParams);
    check(resClockOut, { 'clock-out': (r) => r.status === 200 || r.status === 400 }); // 400 if not clocked in

    const resToday = http.get(`${BASE_URL}/attendance/today`, authParams);
    check(resToday, { 'get today attendance': (r) => r.status === 200 });

    const resStatus = http.get(`${BASE_URL}/attendance/status`, authParams);
    check(resStatus, { 'get attendance status': (r) => r.status === 200 });

    const resHistory = http.get(`${BASE_URL}/attendance/history?page=0&size=10`, authParams);
    check(resHistory, { 'get attendance history': (r) => r.status === 200 });
  });

  group('Leave Management', function () {
    const leaveRequestPayload = JSON.stringify({
        leaveType: 'ANNUAL',
        startDate: '2025-11-10',
        endDate: '2025-11-11',
        reason: 'Vacation'
    });
    const resReq = http.post(`${BASE_URL}/leave/request`, leaveRequestPayload, authParams);
    const leaveRequestId = resReq.status === 201 ? resReq.json('id') : 1;
    check(resReq, { 'submit leave request': (r) => r.status === 201 || r.status === 400 });

    const resMyReqs = http.get(`${BASE_URL}/leave/my-requests`, authParams);
    check(resMyReqs, { 'get my leave requests': (r) => r.status === 200 });

    const resCurrent = http.get(`${BASE_URL}/leave/current`, authParams);
    check(resCurrent, { 'get current leave': (r) => r.status === 200 });

    // Supervisor actions - these might fail if the user is not a supervisor
    const resApprove = http.post(`${BASE_URL}/leave/supervisor/approve/${leaveRequestId}`, '{}', authParams);
    check(resApprove, { 'approve leave': (r) => r.status === 200 || r.status === 403 || r.status === 404 });

    const resReject = http.post(`${BASE_URL}/leave/supervisor/reject/${leaveRequestId}`, '{}', authParams);
    check(resReject, { 'reject leave': (r) => r.status === 200 || r.status === 403 || r.status === 404 });
  });

  group('Business Travel', function () {
    const travelRequestPayload = JSON.stringify({
        destination: 'New York',
        description: 'Client meeting',
        startDate: '2025-12-01',
        endDate: '2025-12-05',
        estimatedCost: 2500.00
    });
    const resReq = http.post(`${BASE_URL}/business-travel/request`, travelRequestPayload, authParams);
    const travelRequestId = resReq.status === 201 ? resReq.json('id') : 1;
    check(resReq, { 'submit travel request': (r) => r.status === 201 || r.status === 400 });

    const resMyReqs = http.get(`${BASE_URL}/business-travel/my-requests`, authParams);
    check(resMyReqs, { 'get my travel requests': (r) => r.status === 200 });

    // Supervisor actions
    const resApprove = http.post(`${BASE_URL}/business-travel/supervisor/approve/${travelRequestId}`, '{}', authParams);
    check(resApprove, { 'approve travel': (r) => r.status === 200 || r.status === 403 || r.status === 404 });

    const resReject = http.post(`${BASE_URL}/business-travel/supervisor/reject/${travelRequestId}`, '{}', authParams);
    check(resReject, { 'reject travel': (r) => r.status === 200 || r.status === 403 || r.status === 404 });
  });

  group('HR Admin', function () {
    const email = `${randomString(10)}@hris.com`;
    const registerPayload = JSON.stringify({
        firstName: 'Test',
        lastName: 'User',
        email: email,
        password: 'password123',
        phoneNumber: '1234567890',
        departmentId: 1,
        positionId: 1,
        hireDate: '2025-10-01',
        faceTemplate: 'mock-face-template'
    });
    const resReg = http.post(`${BASE_URL}/admin/register-employee`, registerPayload, authParams);
    const employeeId = resReg.status === 201 ? resReg.json('id') : 1;
    check(resReg, { 'register employee': (r) => r.status === 201 || r.status === 403 });

    const resAllEmps = http.get(`${BASE_URL}/admin/employees`, authParams);
    check(resAllEmps, { 'get all employees': (r) => r.status === 200 || r.status === 403 });

    const updatePayload = JSON.stringify({ firstName: 'Updated' });
    const resUpdate = http.put(`${BASE_URL}/admin/employees/${employeeId}`, updatePayload, authParams);
    check(resUpdate, { 'update employee': (r) => r.status === 200 || r.status === 403 || r.status === 404 });

    const resDeactivate = http.post(`${BASE_URL}/admin/employees/${employeeId}/deactivate`, '{}', authParams);
    check(resDeactivate, { 'deactivate employee': (r) => r.status === 200 || r.status === 403 || r.status === 404 });

    const facePayload = JSON.stringify({ faceTemplate: 'new-mock-face-template' });
    const resFace = http.post(`${BASE_URL}/admin/employees/${employeeId}/face-template`, facePayload, authParams);
    check(resFace, { 'update face template': (r) => r.status === 200 || r.status === 403 || r.status === 404 });
  });

  sleep(1);
}
