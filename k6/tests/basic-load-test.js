import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { options } from '../config/config.js';

// Custom metrics
const successRate = new Rate('success_rate');
const errorRate = new Rate('error_rate');
const rateLimitExceeded = new Rate('rate_limit_exceeded');

// Load test data
const testUsers = JSON.parse(open('../data/test-users.json'));

// Test configuration
export const options = {
  ...options,
  scenarios: {
    ...options.scenarios.basic_load,
  },
  thresholds: {
    ...options.thresholds,
    'http_req_duration{scenario:basic_load}': ['p(95)<500'],
    'http_req_failed{scenario:basic_load}': ['rate<0.05'],
    'success_rate': ['rate>0.95'],
    'error_rate': ['rate<0.05'],
  },
};

// Helper function to get random user
function getRandomUser() {
  const validUsers = testUsers.filter(user => !user.description.includes('Invalid'));
  return validUsers[Math.floor(Math.random() * validUsers.length)];
}

// Helper function to get invalid user (for negative testing)
function getInvalidUser() {
  const invalidUsers = testUsers.filter(user =>
    user.description.includes('Invalid') || user.description.includes('Non-existent')
  );
  return invalidUsers[Math.floor(Math.random() * invalidUsers.length)];
}

export default function () {
  const baseUrl = options.baseUrl || 'http://localhost:8081';
  const url = `${baseUrl}/api/auth/login`;

  // 80% valid logins, 20% invalid (to test error handling)
  const user = Math.random() < 0.8 ? getRandomUser() : getInvalidUser();

  const payload = {
    email: user.email,
    password: user.password,
  };

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    },
    timeout: '30s',
  };

  // Measure request duration
  const startTime = Date.now();
  const response = http.post(url, JSON.stringify(payload), params);
  const endTime = Date.now();
  const requestDuration = endTime - startTime;

  // Custom metrics tracking
  const isSuccess = response.status === 200;
  const isRateLimited = response.status === 429;
  const isError = response.status >= 400 && !isRateLimited;

  successRate.add(isSuccess);
  errorRate.add(isError);
  rateLimitExceeded.add(isRateLimited);

  // Assertions
  const checks = check(response, {
    'Status is 200, 401, or 429': (r) => [200, 401, 429].includes(r.status),
    'Response time is acceptable': (r) => requestDuration < 2000,
    'Content-Type is application/json': (r) => {
      const contentType = r.headers['Content-Type'];
      return contentType && contentType.includes('application/json');
    },
    'Response body is valid JSON': (r) => {
      try {
        JSON.parse(r.body);
        return true;
      } catch (e) {
        return false;
      }
    },
  });

  // Additional checks for successful responses
  if (response.status === 200) {
    check(response, {
      'Login response contains token': (r) => {
        const body = JSON.parse(r.body);
        return body.data && body.data.token;
      },
      'Login response contains type': (r) => {
        const body = JSON.parse(r.body);
        return body.data && body.data.type === 'Bearer';
      },
      'Login response contains expiresAt': (r) => {
        const body = JSON.parse(r.body);
        return body.data && body.data.expiresAt;
      },
    });
  }

  // Additional checks for rate limited responses
  if (response.status === 429) {
    check(response, {
      'Rate limit response contains retryAfter': (r) => {
        const body = JSON.parse(r.body);
        return body.retryAfter !== undefined;
      },
      'Rate limit response contains email': (r) => {
        const body = JSON.parse(r.body);
        return body.email !== undefined;
      },
      'Rate limit response contains rateLimitType': (r) => {
        const body = JSON.parse(r.body);
        return body.rateLimitType !== undefined;
      },
    });
  }

  // Logging for debugging
  if (__ENV.DEBUG === 'true' || response.status >= 500) {
    console.log(`Status: ${response.status}`);
    console.log(`Duration: ${requestDuration}ms`);
    console.log(`Response: ${response.body.substring(0, 200)}...`);
    console.log(`---`);
  }

  // Sleep between requests to simulate realistic user behavior
  sleep(Math.random() * 2 + 1); // 1-3 seconds between requests
}

export function handleSummary(data) {
  return {
    'raw-data.json': JSON.stringify(data),
    stdout: `
=== Basic Load Test Summary ===
Total Requests: ${data.metrics.http_reqs.values.count}
Success Rate: ${(successRate.count * 100).toFixed(2)}%
Error Rate: ${(errorRate.count * 100).toFixed(2)}%
Rate Limited: ${(rateLimitExceeded.count * 100).toFixed(2)}%

Response Times:
  Min: ${data.metrics.http_req_duration.values.min.toFixed(2)}ms
  Max: ${data.metrics.http_req_duration.values.max.toFixed(2)}ms
  Median: ${data.metrics.http_req_duration.values.med.toFixed(2)}ms
  90th: ${data.metrics.http_req_duration.values.p(90).toFixed(2)}ms
  95th: ${data.metrics.http_req_duration.values.p(95).toFixed(2)}ms

Status Codes:
${Object.entries(data.metrics.http_reqs.values.counts).map(([status, count]) =>
  `  ${status}: ${count} (${(count/data.metrics.http_reqs.values.count*100).toFixed(1)}%)`
).join('\n')}
    `,
  };
}