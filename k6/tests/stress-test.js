import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { options } from '../config/config.js';

// Custom metrics for stress testing
const throughput = new Rate('throughput');
const errorRate = new Rate('stress_error_rate');
const responseTime = new Trend('stress_response_time');
const systemErrors = new Counter('system_errors');

// Load test data
const testUsers = JSON.parse(open('../data/test-users.json'));

// Stress test configuration
export const options = {
  ...options,
  scenarios: {
    // Stress test to find breaking point
    stress_test: {
      executor: 'ramping-vus',
      stages: [
        { duration: '30s', target: 50 },   // Warm up to 50 VUs
        { duration: '1m', target: 100 },   // Ramp up to 100 VUs
        { duration: '30s', target: 150 },  // Push to 150 VUs
        { duration: '1m', target: 200 },   // Push to 200 VUs
        { duration: '30s', target: 250 },  // Push to 250 VUs
        { duration: '1m', target: 250 },   // Hold at 250 VUs
        { duration: '30s', target: 300 },  // Push to 300 VUs
        { duration: '1m', target: 300 },   // Hold at 300 VUs
        { duration: '30s', target: 0 },    // Ramp down
      ],
      startTime: '0s',
    },
  },

  thresholds: {
    // More lenient thresholds for stress testing
    'http_req_duration{scenario:stress_test}': ['p(95)<2000'], // Allow 2s for 95% under stress
    'http_req_failed{scenario:stress_test}': ['rate<0.15'],   // Allow up to 15% failures under stress
    'stress_response_time': ['p(90)<1500'],                  // 90% under 1.5s
    'system_errors': ['count<10'],                           // Minimal system errors
  },
};

// Helper function to get random user with diversity
function getRandomUser(vuId, iteration) {
  const validUsers = testUsers.filter(user => !user.description.includes('Invalid'));
  const userIndex = (vuId + iteration) % validUsers.length;
  return validUsers[userIndex];
}

// Helper function to get some invalid users for error testing
function getInvalidUser() {
  const invalidUsers = testUsers.filter(user =>
    user.description.includes('Invalid') || user.description.includes('Non-existent')
  );
  return invalidUsers[Math.floor(Math.random() * invalidUsers.length)];
}

export default function () {
  const baseUrl = options.baseUrl || 'http://localhost:8081';
  const url = `${baseUrl}/api/auth/login`;

  // Mix of valid and invalid users (90% valid, 10% invalid)
  const user = Math.random() < 0.9 ? getRandomUser(__VU, __ITER) : getInvalidUser();

  const payload = {
    email: user.email,
    password: user.password,
  };

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'User-Agent': `k6-stress-test-vu${__VU}-iter${__ITER}`,
      'X-Test-Scenario': 'stress-test',
    },
    timeout: '10s', // Shorter timeout for stress testing
  };

  const startTime = Date.now();
  const response = http.post(url, JSON.stringify(payload), params);
  const endTime = Date.now();
  const requestDuration = endTime - startTime;

  // Track metrics
  responseTime.add(requestDuration);
  const isSuccess = response.status === 200;
  const isRateLimited = response.status === 429;
  const isSystemError = response.status >= 500;

  throughput.add(isSuccess);
  errorRate.add(!isSuccess && !isRateLimited);

  if (isSystemError) {
    systemErrors.add(1);
  }

  // Basic health checks
  const checks = check(response, {
    'Response received': (r) => r.status !== 0,
    'Response time is reasonable': (r) => requestDuration < 5000,
    'Not a server error': (r) => r.status < 500,
    'Valid status code': (r) => [200, 401, 429].includes(r.status),
  });

  // Enhanced checks for successful responses
  if (response.status === 200) {
    check(response, {
      'Success response is complete': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.data && body.data.token && body.data.type && body.data.expiresAt;
        } catch (e) {
          return false;
        }
      },
      'Response time is good': (r) => requestDuration < 1000,
    });
  }

  // Enhanced checks for rate limited responses
  if (response.status === 429) {
    check(response, {
      'Rate limit response is proper': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.retryAfter && body.message && body.rateLimitType;
        } catch (e) {
          return false;
        }
      },
      'Rate limit response is fast': (r) => requestDuration < 500,
    });
  }

  // Log problematic responses
  if (__ENV.DEBUG === 'true' || response.status >= 500) {
    console.log(`[${new Date().toISOString()}] VU:${__VU} Iter:${__ITER}`);
    console.log(`Status: ${response.status}, Duration: ${requestDuration}ms`);
    console.log(`User: ${user.email}`);
    if (response.status >= 500) {
      console.error(`System Error Response: ${response.body.substring(0, 200)}`);
    }
  }

  // Minimal sleep for stress testing
  sleep(Math.random() * 0.5 + 0.1); // 0.1-0.6 seconds
}

export function handleSummary(data) {
  const maxVUs = data.metrics.vus.values.max || 0;
  const totalRequests = data.metrics.http_reqs.values.count;
  const successRate = throughput.count > 0 ? (throughput.count / totalRequests * 100) : 0;
  const errorRateValue = errorRate.count > 0 ? (errorRate.count / totalRequests * 100) : 0;

  // Calculate requests per second at peak load
  const rpsValues = data.metrics.http_reqs.values.rates || {};
  const peakRPS = Math.max(...Object.values(rpsValues));

  return {
    'stress-test-results.json': JSON.stringify(data, null, 2),
    stdout: `
=== Stress Test Results Summary ===

Load Characteristics:
  Max Virtual Users: ${maxVUs}
  Total Requests: ${totalRequests}
  Peak Requests/sec: ${peakRPS.toFixed(2)}

Performance Metrics:
  Success Rate: ${successRate.toFixed(2)}%
  Error Rate: ${errorRateValue.toFixed(2)}%
  System Errors: ${systemErrors.count}

Response Times:
  Min: ${responseTime.min ? responseTime.min.toFixed(2) : 'N/A'}ms
  Max: ${responseTime.max ? responseTime.max.toFixed(2) : 'N/A'}ms
  Median: ${responseTime.med ? responseTime.med.toFixed(2) : 'N/A'}ms
  90th: ${responseTime.p(90) ? responseTime.p(90).toFixed(2) : 'N/A'}ms
  95th: ${responseTime.p(95) ? responseTime.p(95).toFixed(2) : 'N/A'}ms

Status Code Distribution:
${Object.entries(data.metrics.http_reqs.values.counts).map(([status, count]) =>
  `  ${status}: ${count} (${(count/totalRequests*100).toFixed(1)}%)`
).join('\n')}

System Health Assessment:
✅ System stayed stable: ${systemErrors.count < 10 ? 'YES' : 'NO'}
✅ Error rate acceptable: ${errorRateValue < 15 ? 'YES' : 'NO'}
✅ Response times acceptable: ${responseTime.p(95) ? responseTime.p(95) < 2000 ? 'YES' : 'NO' : 'NO'}

Breaking Point Analysis:
- Max VUs tested: ${maxVUs}
- System handled load: ${systemErrors.count < 10 && errorRateValue < 20 ? 'YES' : 'NO'}
- Recommended max VUs: ${systemErrors.count === 0 && errorRateValue < 10 ? maxVUs : Math.floor(maxVUs * 0.8)}

Performance Grades:
A: Excellent (≤5% errors, ≤500ms p95)
B: Good (≤10% errors, ≤1s p95)
C: Acceptable (≤15% errors, ≤2s p95)
D: Poor (>15% errors or >2s p95)

Overall Grade: ${
  errorRateValue <= 5 && responseTime.p(95) && responseTime.p(95) <= 500 ? 'A' :
  errorRateValue <= 10 && responseTime.p(95) && responseTime.p(95) <= 1000 ? 'B' :
  errorRateValue <= 15 && responseTime.p(95) && responseTime.p(95) <= 2000 ? 'C' : 'D'
}
    `,
  };
}