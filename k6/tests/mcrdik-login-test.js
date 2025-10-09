import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics for this specific user test
const loginSuccessRate = new Rate('login_success_rate');
const loginErrorRate = new Rate('login_error_rate');
const rateLimitHits = new Rate('rate_limit_hits');
const responseTime = new Trend('login_response_time');
const tokenValidation = new Rate('token_validation');

// Test configuration
export const options = {
  stages: [
    { duration: '10s', target: 5 },   // Warm up
    { duration: '30s', target: 10 },  // Normal load
    { duration: '1m', target: 15 },   // Steady load
    { duration: '30s', target: 5 },   // Cool down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95% under 500ms
    http_req_failed: ['rate<0.1'],     // Less than 10% failures
    login_success_rate: ['rate>0.9'],  // 90% success rate
    login_error_rate: ['rate<0.1'],    // Less than 10% errors
  },
};

// Test constants
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
      'User-Agent': `k6-mcrdik-test-vu${__VU}`,
    },
    timeout: '10s',
  };

  // Track request start time
  const startTime = Date.now();
  const response = http.post(url, payload, params);
  const endTime = Date.now();
  const requestDuration = endTime - startTime;

  // Track metrics
  responseTime.add(requestDuration);

  const isSuccess = response.status === 200;
  const isRateLimited = response.status === 429;
  const isAuthFailed = response.status === 401;
  const isSystemError = response.status >= 500;

  loginSuccessRate.add(isSuccess);
  loginErrorRate.add(isAuthFailed);
  rateLimitHits.add(isRateLimited);

  // Basic checks
  const checks = check(response, {
    'login endpoint responds': (r) => r.status !== 0,
    'response time is acceptable': (r) => requestDuration < 2000,
    'valid status code': (r) => [200, 401, 429].includes(r.status),
    'content-type header exists': (r) => r.headers['Content-Type'] !== undefined,
    'response is valid JSON': (r) => {
      try {
        JSON.parse(r.body);
        return true;
      } catch (e) {
        return false;
      }
    },
  });

  // Enhanced checks for successful login
  if (response.status === 200) {
    const body = JSON.parse(response.body);
    const hasToken = body.data && body.data.token;
    const hasType = body.data && body.data.type === 'Bearer';
    const hasExpiry = body.data && body.data.expiresAt;

    check(response, {
      'login successful': (r) => r.status === 200,
      'response contains token': (r) => hasToken,
      'token type is Bearer': (r) => hasType,
      'response contains expiresAt': (r) => hasExpiry,
      'token is not empty': (r) => hasToken && body.data.token.length > 0,
    });

    if (hasToken && hasType && hasExpiry) {
      tokenValidation.add(1);
    }
  }

  // Checks for rate limiting
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
      'rate limit headers present': (r) => {
        return r.headers['Retry-After'] !== undefined &&
               r.headers['X-RateLimit-Resource'] !== undefined;
      },
    });
  }

  // Logging for debugging and monitoring
  if (__ENV.DEBUG === 'true' || response.status >= 500) {
    console.log(`[${new Date().toISOString()}] VU:${__VU} - Status: ${response.status}, Duration: ${requestDuration}ms`);
    if (response.status === 429) {
      const body = JSON.parse(response.body);
      console.log(`Rate Limited: retryAfter=${body.retryAfter}s, type=${body.rateLimitType}`);
    }
  }

  // Realistic user behavior - wait between requests
  sleep(Math.random() * 2 + 1); // 1-3 seconds
}

export function handleSummary(data) {
  const totalRequests = data.metrics.http_reqs.values.count;
  const successRate = loginSuccessRate.count > 0 ? (loginSuccessRate.count / totalRequests * 100) : 0;
  const errorRate = loginErrorRate.count > 0 ? (loginErrorRate.count / totalRequests * 100) : 0;
  const rateLimitRate = rateLimitHits.count > 0 ? (rateLimitHits.count / totalRequests * 100) : 0;
  const tokenValidationRate = tokenValidation.count > 0 ? (tokenValidation.count / totalRequests * 100) : 0;

  return {
    'mcrdik-login-results.json': JSON.stringify(data, null, 2),
    stdout: `
=== mcrdik@gmail.com Login Test Results ===

Test Configuration:
  Email: ${TEST_EMAIL}
  Base URL: ${BASE_URL}
  Total Requests: ${totalRequests}

Performance Metrics:
  Success Rate: ${successRate.toFixed(2)}%
  Error Rate: ${errorRate.toFixed(2)}%
  Rate Limited: ${rateLimitRate.toFixed(2)}%
  Token Validation: ${tokenValidationRate.toFixed(2)}%

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

Rate Limiting Analysis:
✅ Rate limiting working: ${rateLimitRate > 0 ? 'YES' : 'NO'}
✅ Success rate acceptable: ${successRate >= 90 ? 'YES' : 'NO'}
✅ Response times acceptable: ${responseTime.p(95) && responseTime.p(95) < 500 ? 'YES' : 'NO'}

Recommendations:
${successRate < 90 ? '- Investigate login failures\n' : ''}
${errorRate > 10 ? '- Check authentication credentials\n' : ''}
${rateLimitRate > 20 ? '- Consider request rate optimization\n' : ''}
${responseTime.p(95) && responseTime.p(95) > 500 ? '- Optimize API response times\n' : ''}
    `,
  };
}