import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';
import { options } from '../config/config.js';

// Custom metrics for rate limiting testing
const accountRateLimitHits = new Rate('account_rate_limit_hits');
const ipRateLimitHits = new Rate('ip_rate_limit_hits');
const successfulLogins = new Rate('successful_logins');
const failedLogins = new Rate('failed_logins');
const loginResponseTime = new Trend('login_response_time');
const rateLimitResponseTime = new Trend('rate_limit_response_time');

// Load test data
const testUsers = JSON.parse(open('../data/test-users.json'));

// Rate limiting test configuration
export const options = {
  ...options,
  scenarios: {
    // Test account-based rate limiting (5 per 5 minutes)
    account_rate_limit: {
      executor: 'constant-vus',
      vus: 1,
      duration: '6m', // Run for 6 minutes to see rate limiting reset
      startTime: '0s',
    },

    // Test IP-based rate limiting (20 per 5 minutes)
    ip_rate_limit: {
      executor: 'constant-vus',
      vus: 25, // More than 20 to trigger IP rate limiting
      duration: '6m',
      startTime: '6m', // Start after account test
    },
  },

  thresholds: {
    // We expect rate limiting to kick in, so we allow 429 responses
    'http_req_failed{scenario:account_rate_limit}': ['rate<0.3'], // Up to 30% failures expected
    'http_req_failed{scenario:ip_rate_limit}': ['rate<0.5'],      // Up to 50% failures expected
    'account_rate_limit_hits': ['rate>0.1'],                     // Should see rate limiting
    'ip_rate_limit_hits': ['rate>0.1'],                         // Should see rate limiting
    'login_response_time': ['p(95)<1000'],                      // Successful logins should be fast
    'rate_limit_response_time': ['p(95)<500'],                  // Rate limit responses should be very fast
  },
};

// Helper function to get a single user for account-based testing
function getTestUser() {
  return testUsers.find(user => user.email === 'admin@hris.com') || testUsers[0];
}

// Helper function to get diverse users for IP-based testing
function getDiverseUser(vuId, iteration) {
  const validUsers = testUsers.filter(user => !user.description.includes('Invalid'));
  const userIndex = (vuId + iteration) % validUsers.length;
  return validUsers[userIndex];
}

export default function () {
  const baseUrl = options.baseUrl || 'http://localhost:8081';
  const url = `${baseUrl}/api/auth/login`;
  const scenario = __SCENARIO;

  let user;
  if (scenario === 'account_rate_limit') {
    // Use same user for account-based rate limiting test
    user = getTestUser();
  } else if (scenario === 'ip_rate_limit') {
    // Use different users for IP-based rate limiting test
    user = getDiverseUser(__VU, __ITER);
  }

  const payload = {
    email: user.email,
    password: user.password,
  };

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'User-Agent': `k6-load-test-${__VU}-${__ITER}`,
    },
    timeout: '30s',
  };

  const startTime = Date.now();
  const response = http.post(url, JSON.stringify(payload), params);
  const endTime = Date.now();
  const requestDuration = endTime - startTime;

  // Track response times
  if (response.status === 200) {
    loginResponseTime.add(requestDuration);
  } else if (response.status === 429) {
    rateLimitResponseTime.add(requestDuration);
  }

  // Categorize responses
  const isSuccess = response.status === 200;
  const isRateLimited = response.status === 429;
  const isAuthFailed = response.status === 401;

  successfulLogins.add(isSuccess);
  failedLogins.add(isAuthFailed);

  // Check rate limiting responses
  if (response.status === 429) {
    try {
      const body = JSON.parse(response.body);
      const rateLimitType = body.rateLimitType;

      if (rateLimitType === 'LOGIN_SUCCESS') {
        accountRateLimitHits.add(1);
        console.log(`Account rate limit hit for user: ${user.email}, retryAfter: ${body.retryAfter}s`);
      } else {
        ipRateLimitHits.add(1);
        console.log(`IP rate limit hit, retryAfter: ${body.retryAfter}s, VU: ${__VU}`);
      }
    } catch (e) {
      console.error('Failed to parse rate limit response:', e.message);
    }
  }

  // Comprehensive assertions
  const checks = check(response, {
    'Status is valid (200, 401, or 429)': (r) => [200, 401, 429].includes(r.status),
    'Response time is under 5 seconds': (r) => requestDuration < 5000,
    'Content-Type header exists': (r) => r.headers['Content-Type'] !== undefined,
    'Response is valid JSON': (r) => {
      try {
        JSON.parse(r.body);
        return true;
      } catch (e) {
        return false;
      }
    },
  });

  // Additional checks for different response types
  if (response.status === 200) {
    check(response, {
      'Success response contains token': (r) => {
        const body = JSON.parse(r.body);
        return body.data && body.data.token;
      },
      'Success response type is Bearer': (r) => {
        const body = JSON.parse(r.body);
        return body.data && body.data.type === 'Bearer';
      },
    });
  } else if (response.status === 429) {
    check(response, {
      'Rate limit response has proper structure': (r) => {
        const body = JSON.parse(r.body);
        return body.retryAfter !== undefined &&
               body.message !== undefined &&
               body.rateLimitType !== undefined;
      },
      'Rate limit headers are present': (r) => {
        return r.headers['Retry-After'] !== undefined &&
               r.headers['X-RateLimit-Resource'] !== undefined;
      },
    });
  }

  // Detailed logging for rate limiting analysis
  if (__ENV.DEBUG === 'true' || response.status === 429) {
    console.log(`[${new Date().toISOString()}] VU:${__VU} Iter:${__ITER} - Status: ${response.status}, Duration: ${requestDuration}ms`);
    if (response.status === 429) {
      const body = JSON.parse(response.body);
      console.log(`Rate Limit Details: ${JSON.stringify({
        email: body.email,
        retryAfter: body.retryAfter,
        rateLimitType: body.rateLimitType,
        message: body.message
      })}`);
    }
  }

  // Sleep between requests (shorter for rate limiting tests)
  sleep(Math.random() * 2 + 0.5); // 0.5-2.5 seconds
}

export function handleSummary(data) {
  const accountRateLimitHitRate = accountRateLimitHits.count > 0 ? (accountRateLimitHits.count / data.metrics.http_reqs.values.count * 100) : 0;
  const ipRateLimitHitRate = ipRateLimitHits.count > 0 ? (ipRateLimitHits.count / data.metrics.http_reqs.values.count * 100) : 0;

  return {
    'rate-limit-summary.json': JSON.stringify(data, null, 2),
    stdout: `
=== Rate Limiting Test Summary ===

Account Rate Limiting:
  Account Rate Limit Hits: ${(accountRateLimitHitRate).toFixed(2)}%
  Expected: Should see rate limiting after 5 successful logins per 5 minutes

IP Rate Limiting:
  IP Rate Limit Hits: ${(ipRateLimitHitRate).toFixed(2)}%
  Expected: Should see rate limiting after 20 successful logins per 5 minutes

Response Times:
  Successful Logins:
    Min: ${loginResponseTime.min ? loginResponseTime.min.toFixed(2) : 'N/A'}ms
    Max: ${loginResponseTime.max ? loginResponseTime.max.toFixed(2) : 'N/A'}ms
    95th: ${loginResponseTime.p(95) ? loginResponseTime.p(95).toFixed(2) : 'N/A'}ms

  Rate Limited Responses:
    Min: ${rateLimitResponseTime.min ? rateLimitResponseTime.min.toFixed(2) : 'N/A'}ms
    Max: ${rateLimitResponseTime.max ? rateLimitResponseTime.max.toFixed(2) : 'N/A'}ms
    95th: ${rateLimitResponseTime.p(95) ? rateLimitResponseTime.p(95).toFixed(2) : 'N/A'}ms

Total Requests: ${data.metrics.http_reqs.values.count}
Successful Logins: ${(successfulLogins.count * 100).toFixed(2)}%
Failed Logins: ${(failedLogins.count * 100).toFixed(2)}%

Status Code Distribution:
${Object.entries(data.metrics.http_reqs.values.counts).map(([status, count]) =>
  `  ${status}: ${count} (${(count/data.metrics.http_reqs.values.count*100).toFixed(1)}%)`
).join('\n')}

Rate Limiting Effectiveness:
✅ Account rate limiting working: ${accountRateLimitHitRate > 0 ? 'YES' : 'NO'}
✅ IP rate limiting working: ${ipRateLimitHitRate > 0 ? 'YES' : 'NO'}
    `,
  };
}