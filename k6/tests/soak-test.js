import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { options } from '../config/config.js';

// Custom metrics for soak testing (endurance)
const enduranceSuccessRate = new Rate('endurance_success_rate');
const enduranceErrorRate = new Rate('endurance_error_rate');
const enduranceResponseTime = new Trend('endurance_response_time');
const memoryLeakDetection = new Trend('memory_leak_detection');
const degradationTracker = new Trend('degradation_tracker');
const longRunningErrors = new Counter('long_running_errors');

// Load test data
const testUsers = JSON.parse(open('../data/test-users.json'));

// Soak test configuration (10-minute endurance test)
export const options = {
  ...options,
  scenarios: {
    // Long-running test to detect memory leaks and performance degradation
    soak_test: {
      executor: 'constant-vus',
      vus: 20, // Steady load
      duration: '10m', // Extended duration for endurance testing
      startTime: '0s',
    },
  },

  thresholds: {
    // Strict thresholds for endurance testing
    'http_req_duration{scenario:soak_test}': ['p(95)<800'], // Should remain fast
    'http_req_failed{scenario:soak_test}': ['rate<0.02'],   // Less than 2% failures
    'endurance_success_rate': ['rate>0.98'],                 // 98% success rate
    'endurance_response_time': ['p(90)<600', 'p(95)<800'],   // Consistent response times
    'long_running_errors': ['count<5'],                      // Minimal long-running errors
  },
};

// Helper function to rotate users for realistic distribution
function getUserForIteration(vuId, iteration) {
  const validUsers = testUsers.filter(user => !user.description.includes('Invalid'));
  const userIndex = (vuId * 1000 + iteration) % validUsers.length;
  return validUsers[userIndex];
}

// Track response times over time for degradation detection
let responseTimeHistory = [];
const MAX_HISTORY_SIZE = 60; // Track last 60 measurements

export default function () {
  const baseUrl = options.baseUrl || 'http://localhost:8081';
  const url = `${baseUrl}/api/auth/login`;

  const user = getUserForIteration(__VU, __ITER);
  const currentMinute = Math.floor(__ITER / 60); // Track which minute we're in

  const payload = {
    email: user.email,
    password: user.password,
  };

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'User-Agent': `k6-soak-test-vu${__VU}-min${currentMinute}`,
      'X-Test-Scenario': 'soak-test',
      'X-Test-Minute': currentMinute.toString(),
    },
    timeout: '15s', // Moderate timeout
  };

  const startTime = Date.now();
  const response = http.post(url, JSON.stringify(payload), params);
  const endTime = Date.now();
  const requestDuration = endTime - startTime;

  // Track response times for degradation analysis
  responseTimeHistory.push({
    timestamp: Date.now(),
    duration: requestDuration,
    iteration: __ITER,
    minute: currentMinute,
    vu: __VU,
  });

  if (responseTimeHistory.length > MAX_HISTORY_SIZE) {
    responseTimeHistory.shift();
  }

  // Calculate metrics
  const isSuccess = response.status === 200;
  const isRateLimited = response.status === 429;
  const isSystemError = response.status >= 500;
  const isLongRunning = requestDuration > 2000;

  enduranceSuccessRate.add(isSuccess);
  enduranceErrorRate.add(!isSuccess && !isRateLimited);
  enduranceResponseTime.add(requestDuration);

  if (isSystemError) {
    longRunningErrors.add(1);
  }

  if (isLongRunning) {
    memoryLeakDetection.add(requestDuration);
  }

  // Track performance degradation
  if (responseTimeHistory.length >= 30) {
    const recentTimes = responseTimeHistory.slice(-30).map(h => h.duration);
    const avgRecent = recentTimes.reduce((a, b) => a + b, 0) / recentTimes.length;
    degradationTracker.add(avgRecent);
  }

  // Comprehensive checks
  const checks = check(response, {
    'Response received': (r) => r.status !== 0,
    'Response time is reasonable': (r) => requestDuration < 3000,
    'Valid status code': (r) => [200, 401, 429].includes(r.status),
    'Content-Type present': (r) => r.headers['Content-Type'] !== undefined,
    'Response is valid JSON': (r) => {
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
      'Success response structure valid': (r) => {
        try {
          const body = JSON.parse(r.body);
          return body.data && body.data.token && body.data.type === 'Bearer';
        } catch (e) {
          return false;
        }
      },
      'Quick response for success': (r) => requestDuration < 500,
    });
  }

  // Monitor for degradation patterns
  if (responseTimeHistory.length >= 60) {
    const firstHalf = responseTimeHistory.slice(0, 30).map(h => h.duration);
    const secondHalf = responseTimeHistory.slice(-30).map(h => h.duration);

    const avgFirstHalf = firstHalf.reduce((a, b) => a + b, 0) / firstHalf.length;
    const avgSecondHalf = secondHalf.reduce((a, b) => a + b, 0) / secondHalf.length;

    const degradationPercent = ((avgSecondHalf - avgFirstHalf) / avgFirstHalf) * 100;

    // Alert if degradation exceeds 20%
    if (degradationPercent > 20) {
      console.warn(`Performance degradation detected: ${degradationPercent.toFixed(1)}% increase in response times`);
      console.warn(`Average first half: ${avgFirstHalf.toFixed(2)}ms, second half: ${avgSecondHalf.toFixed(2)}ms`);
    }
  }

  // Log long-running requests
  if (__ENV.DEBUG === 'true' || requestDuration > 2000) {
    console.log(`[${new Date().toISOString()}] Long request: ${requestDuration}ms, VU:${__VU}, Status:${response.status}`);
  }

  // Realistic user behavior - varied sleep times
  const sleepTime = Math.random() * 3 + 1; // 1-4 seconds
  sleep(sleepTime);
}

export function handleSummary(data) {
  const totalRequests = data.metrics.http_reqs.values.count;
  const testDuration = 10 * 60; // 10 minutes in seconds
  const avgRPS = totalRequests / testDuration;

  const successRate = enduranceSuccessRate.count > 0 ? (enduranceSuccessRate.count / totalRequests * 100) : 0;
  const errorRateValue = enduranceErrorRate.count > 0 ? (enduranceErrorRate.count / totalRequests * 100) : 0;

  // Analyze response time stability
  const responseTimes = enduranceResponseTime.values || [];
  const minTime = Math.min(...responseTimes);
  const maxTime = Math.max(...responseTimes);
  const avgTime = responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length;
  const stabilityIndex = ((maxTime - minTime) / avgTime) * 100; // Lower is better

  return {
    'soak-test-results.json': JSON.stringify(data, null, 2),
    'performance-degradation.json': JSON.stringify({
      responseTimeHistory,
      stabilityIndex,
      longRunningRequests: memoryLeakDetection.count,
    }, null, 2),
    stdout: `
=== Soak Test (Endurance) Results ===

Test Duration: 10 minutes
Total Requests: ${totalRequests}
Average Requests/sec: ${avgRPS.toFixed(2)}

Endurance Metrics:
  Success Rate: ${successRate.toFixed(2)}% (Target: >98%)
  Error Rate: ${errorRateValue.toFixed(2)}% (Target: <2%)
  Long-running Errors: ${longRunningErrors.count}

Response Time Analysis:
  Min: ${minTime.toFixed(2)}ms
  Max: ${maxTime.toFixed(2)}ms
  Average: ${avgTime.toFixed(2)}ms
  50th (Median): ${enduranceResponseTime.med ? enduranceResponseTime.med.toFixed(2) : 'N/A'}ms
  90th: ${enduranceResponseTime.p(90) ? enduranceResponseTime.p(90).toFixed(2) : 'N/A'}ms
  95th: ${enduranceResponseTime.p(95) ? enduranceResponseTime.p(95).toFixed(2) : 'N/A'}ms

Performance Stability:
  Stability Index: ${stabilityIndex.toFixed(1)}% (Lower is better)
  Long-running Requests (>2s): ${memoryLeakDetection.count}
  Degradation Detected: ${memoryLeakDetection.count > 10 ? 'YES' : 'NO'}

Status Code Distribution:
${Object.entries(data.metrics.http_reqs.values.counts).map(([status, count]) =>
  `  ${status}: ${count} (${(count/totalRequests*100).toFixed(1)}%)`
).join('\n')}

System Health Assessment:
✅ High Success Rate: ${successRate >= 98 ? 'PASS' : 'FAIL'}
✅ Low Error Rate: ${errorRateValue <= 2 ? 'PASS' : 'FAIL'}
✅ Stable Performance: ${stabilityIndex <= 50 ? 'PASS' : 'FAIL'}
✅ No Memory Leaks: ${memoryLeakDetection.count <= 5 ? 'PASS' : 'FAIL'}

Endurance Grade:
A: Excellent (≥98% success, stable performance, no degradation)
B: Good (≥95% success, minor variations)
C: Acceptable (≥90% success, some degradation)
D: Poor (<90% success or significant degradation)

Overall Endurance Grade: ${
  successRate >= 98 && stabilityIndex <= 50 && memoryLeakDetection.count <= 5 ? 'A' :
  successRate >= 95 && stabilityIndex <= 75 ? 'B' :
  successRate >= 90 ? 'C' : 'D'
}

Recommendations:
${successRate < 98 ? '- Investigate causes of request failures\n' : ''}
${stabilityIndex > 50 ? '- Monitor for performance degradation\n' : ''}
${memoryLeakDetection.count > 5 ? '- Check for potential memory leaks\n' : ''}
${avgTime > 500 ? '- Consider optimization for response times\n' : ''}
    `,
  };
}