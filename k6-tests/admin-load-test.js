import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

export let errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '1m', target: 2 }, // Ramp up to 2 admin users
    { duration: '2m', target: 2 }, // Stay at 2 admin users
    { duration: '1m', target: 5 }, // Ramp up to 5 admin users
    { duration: '2m', target: 5 }, // Stay at 5 admin users
    { duration: '1m', target: 0 }, // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<5000'], // 95% of requests under 5s (admin operations can be slower)
    http_req_failed: ['rate<0.2'], // Error rate under 20%
    errors: ['rate<0.2'],
  },
};

const BASE_URL = 'http://localhost:8081';

// Sample employee data for registration
const employeeTemplates = [
  {
    firstName: 'John',
    lastName: 'Doe',
    departmentId: 1,
    positionId: 1,
    supervisorId: 1,
    hireDate: '2024-01-15',
    annualLeaveBalance: 12,
    sickLeaveBalance: 10,
  },
  {
    firstName: 'Jane',
    lastName: 'Smith',
    departmentId: 2,
    positionId: 2,
    supervisorId: 2,
    hireDate: '2024-02-01',
    annualLeaveBalance: 12,
    sickLeaveBalance: 10,
  },
  {
    firstName: 'Michael',
    lastName: 'Johnson',
    departmentId: 1,
    positionId: 3,
    supervisorId: 1,
    hireDate: '2024-03-15',
    annualLeaveBalance: 12,
    sickLeaveBalance: 10,
  },
];

export default function () {
  const adminToken = 'dummy-admin-token';
  const adminHeaders = {
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json',
    },
  };

  // Test get all employees
  const employeesResponse = http.get(`${BASE_URL}/api/admin/employees`, adminHeaders);

  check(employeesResponse, {
    'get employees status is 200': (r) => r.status === 200,
    'get employees response time < 2000ms': (r) => r.timings.duration < 2000,
  }) || errorRate.add(1);

  sleep(1);

  // Test employee registration
  const template = employeeTemplates[Math.floor(Math.random() * employeeTemplates.length)];
  const timestamp = Date.now();

  const employeePayload = JSON.stringify({
    ...template,
    email: `${template.firstName.toLowerCase()}.${template.lastName.toLowerCase()}.${timestamp}@company.com`,
    phoneNumber: `+123456789${timestamp % 100}`,
    password: 'tempPassword123',
  });

  const registerResponse = http.post(`${BASE_URL}/api/admin/register-employee`, employeePayload, adminHeaders);

  check(registerResponse, {
    'register employee status is 200 or 400': (r) => r.status === 200 || r.status === 400,
    'register employee response time < 5000ms': (r) => r.timings.duration < 5000,
  }) || errorRate.add(1);

  sleep(1);

  // Test employee update (assuming employee ID 1 exists)
  const updatePayload = JSON.stringify({
    firstName: 'Updated',
    lastName: 'Name',
    phoneNumber: '+9876543210',
    annualLeaveBalance: 15,
    sickLeaveBalance: 12,
  });

  const updateResponse = http.put(`${BASE_URL}/api/admin/employees/1`, updatePayload, adminHeaders);

  check(updateResponse, {
    'update employee status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'update employee response time < 3000ms': (r) => r.timings.duration < 3000,
  }) || errorRate.add(1);

  sleep(1);

  // Test employee deactivation
  const deactivateResponse = http.post(`${BASE_URL}/api/admin/employees/999/deactivate`, null, adminHeaders);

  check(deactivateResponse, {
    'deactivate employee status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'deactivate employee response time < 2000ms': (r) => r.timings.duration < 2000,
  }) || errorRate.add(1);

  sleep(1);

  // Test face template update
  const faceTemplatePayload = JSON.stringify({
    faceTemplate: 'base64-encoded-face-template-data-for-load-testing',
  });

  const faceTemplateResponse = http.post(`${BASE_URL}/api/admin/employees/1/face-template`, faceTemplatePayload, adminHeaders);

  check(faceTemplateResponse, {
    'face template status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'face template response time < 4000ms': (r) => r.timings.duration < 4000,
  }) || errorRate.add(1);

  sleep(1);
}