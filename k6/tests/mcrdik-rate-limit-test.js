import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

// Custom metrics for rate limiting test
const rateLimitHits = new Rate('rate_limit_hits');
const successfulLogins = new Rate('successful_logins');
const rateLimitResponseTime = new Counter('rate_limit_response_time');

// Test rate limiting for this specific user
export const options = {
  // Constant VUs to trigger rate limiting quickly
  vus: 1,
  duration: '6m', // 6 minutes to see rate limit reset
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    rate_limit_hits: ['rate>0.1'], // Should see rate limiting
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const TEST_EMAIL = 'mcrdik@gmail.com';
const TEST_PASSWORD = 'week123';

export default function () {
  const url = `${BASE_URL}/api/auth/login`;

  const payload = JSON.stringify({
    email: TEST_EMAIL,
    password: TEST_PASSWORD,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'User-Agent': `k6-rate-limit-test-vu${__VU}`,
    },
  };

  const startTime = Date.now();
  const response = http.post(url, payload, params);
  const endTime = Date.now();
  const requestDuration = endTime - startTime;

  const isSuccess = response.status === 200;
  const isRateLimited = response.status === 429;

  successfulLogins.add(isSuccess);
  rateLimitHits.add(isRateLimited);

  if (isRateLimited) {
    rateLimitResponseTime.add(requestDuration);
  }

  check(response, {
    'status is valid': (r) => [200, 401, 429].includes(r.status),
    'response time acceptable': (r) => requestDuration < 5000,
  });

  if (response.status === 429) {
    check(response, {
      'rate limit response structure': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.retryAfter !== undefined &&
                 body.message !== undefined &&
                 body.rateLimitType !== undefined;
        } catch (e) {
          return false;
        }
      },
    });

    // Log rate limiting details
    const body = JSON.parse(response.body);
    console.log(`[${new Date().toISOString()}] Rate limited: retryAfter=${body.retryAfter}s, type=${body.rateLimitType}`);
  }

  // Fast requests to trigger rate limiting
  sleep(0.5);
}

export function handleSummary(data) {
  const totalRequests = data.metrics.http_reqs.values.count;
  const successRate = successfulLogins.count > 0 ? (successfulLogins.count / totalRequests * 100) : 0;
  const rateLimitRate = rateLimitHits.count > 0 ? (rateLimitHits.count / totalRequests * 100) : 0;

  return {
    'mcrdik-rate-limit-results.json': JSON.stringify(data, null, 2),
    stdout: `
=== mcrdik@gmail.com Rate Limiting Test Results ===

Configuration:
  Email: ${TEST_EMAIL}
  Test Duration: 6 minutes
  Request Interval: 0.5 seconds

Results:
  Total Requests: ${totalRequests}
  Successful Logins: ${successRate.toFixed(2)}%
  Rate Limited: ${rateLimitRate.toFixed(2)}%

Rate Limiting Effectiveness:
✅ Rate limiting active: ${rateLimitRate > 0 ? 'YES' : 'NO'}
✅ Expected rate limit: 5 successful logins per 5 minutes per account

Note: You should see 429 Too Many Requests responses after 5 successful logins.
Rate limits should reset after 5 minutes.
    `,
  };
}