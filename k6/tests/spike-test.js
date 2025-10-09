import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { options } from '../config/config.js';

// Custom metrics for spike testing
const spikeRecoveryRate = new Rate('spike_recovery_rate');
const spikeHandlingRate = new Rate('spike_handling_rate');
const spikeResponseTime = new Trend('spike_response_time');
const spikeErrors = new Counter('spike_errors');
const recoveryTimeTracker = new Trend('recovery_time_tracker');

// Load test data
const testUsers = JSON.parse(open('../data/test-users.json'));

// Spike test configuration
export const options = {
  ...options,
  scenarios: {
    // Spike test - sudden traffic surge
    spike_test: {
      executor: 'ramping-vus',
      stages: [
        { duration: '10s', target: 5 },    // Normal baseline
        { duration: '10s', target: 5 },    // Maintain baseline
        { duration: '10s', target: 100 },  // Spike up to 100 VUs
        { duration: '30s', target: 100 },  // Hold spike
        { duration: '10s', target: 150 },  // Push spike higher
        { duration: '20s', target: 150 },  // Hold higher spike
        { duration: '10s', target: 5 },    // Rapid drop to normal
        { duration: '30s', target: 5 },    // Recovery period
        { duration: '10s', target: 0 },    // Ramp down
      ],
      startTime: '0s',
    },
  },

  thresholds: {
    // More lenient during spike, strict during recovery
    'http_req_duration{scenario:spike_test}': ['p(95)<1500'], // Allow slower during spike
    'spike_response_time': ['p(90)<1000', 'p(95)<1500'],     // Spike performance
    'spike_recovery_rate': ['rate>0.90'],                    // Should recover well
    'spike_handling_rate': ['rate>0.70'],                    // Should handle most spike requests
    'spike_errors': ['count<50'],                           // Limited errors during spike
  },
};

// Track spike phases for analysis
const SPIKE_PHASES = {
  BASELINE_1: { start: 0, end: 20, name: 'Baseline 1' },
  SPIKE_RISE: { start: 20, end: 30, name: 'Spike Rise' },
  SPIKE_HOLD: { start: 30, end: 60, name: 'Spike Hold' },
  SPIKE_HIGH: { start: 60, end: 80, name: 'High Spike' },
  RECOVERY: { start: 80, end: 110, name: 'Recovery' },
  COOLDOWN: { start: 110, end: 120, name: 'Cooldown' },
};

// Performance tracking by phase
let phaseMetrics = {};
Object.keys(SPIKE_PHASES).forEach(phase => {
  phaseMetrics[phase] = {
    requests: 0,
    successes: 0,
    errors: 0,
    totalDuration: 0,
  };
});

function getCurrentPhase(iteration, testStartTime) {
  const testElapsed = (Date.now() - testStartTime) / 1000;

  for (const [phaseKey, phase] of Object.entries(SPIKE_PHASES)) {
    if (testElapsed >= phase.start && testElapsed < phase.end) {
      return phaseKey;
    }
  }
  return 'UNKNOWN';
}

// Helper function to distribute users across VUs
function getUserForVu(vuId, iteration) {
  const validUsers = testUsers.filter(user => !user.description.includes('Invalid'));
  const userIndex = (vuId * 1000 + iteration) % validUsers.length;
  return validUsers[userIndex];
}

let testStartTime;
export function setup() {
  testStartTime = Date.now();
  return { testStartTime };
}

export default function(data) {
  const baseUrl = options.baseUrl || 'http://localhost:8081';
  const url = `${baseUrl}/api/auth/login`;

  const user = getUserForVu(__VU, __ITER);
  const currentPhase = getCurrentPhase(__ITER, testStartTime);

  const payload = {
    email: user.email,
    password: user.password,
  };

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'User-Agent': `k6-spike-test-vu${__VU}-phase${currentPhase}`,
      'X-Test-Scenario': 'spike-test',
      'X-Test-Phase': currentPhase,
    },
    timeout: '10s',
  };

  const requestStartTime = Date.now();
  const response = http.post(url, JSON.stringify(payload), params);
  const requestEndTime = Date.now();
  const requestDuration = requestEndTime - requestStartTime;

  // Track metrics by phase
  if (phaseMetrics[currentPhase]) {
    phaseMetrics[currentPhase].requests++;
    phaseMetrics[currentPhase].totalDuration += requestDuration;

    if (response.status === 200) {
      phaseMetrics[currentPhase].successes++;
    } else if (response.status >= 500) {
      phaseMetrics[currentPhase].errors++;
    }
  }

  // Calculate overall metrics
  const isSuccess = response.status === 200;
  const isRateLimited = response.status === 429;
  const isSystemError = response.status >= 500;

  spikeHandlingRate.add(isSuccess);
  spikeResponseTime.add(requestDuration);

  if (isSystemError) {
    spikeErrors.add(1);
  }

  // Track recovery performance specifically
  if (currentPhase === 'RECOVERY') {
    spikeRecoveryRate.add(isSuccess);
    recoveryTimeTracker.add(requestDuration);
  }

  // Basic health checks
  const checks = check(response, {
    'Response received': (r) => r.status !== 0,
    'Response time under 5s': (r) => requestDuration < 5000,
    'Valid status code': (r) => [200, 401, 429].includes(r.status),
    'Not a server error': (r) => r.status < 500,
  });

  // Enhanced checks during different phases
  if (currentPhase.includes('SPIKE')) {
    check(response, {
      'Spike response acceptable': (r) => requestDuration < 3000,
      'Spike system stable': (r) => r.status < 500,
    });
  } else if (currentPhase === 'RECOVERY') {
    check(response, {
      'Recovery response good': (r) => requestDuration < 1000,
      'Recovery successful': (r) => r.status === 200,
    });
  }

  // Log interesting events
  if (__ENV.DEBUG === 'true' || response.status >= 500 || requestDuration > 3000) {
    console.log(`[${new Date().toISOString()}] Phase: ${currentPhase}, VU:${__VU}`);
    console.log(`Status: ${response.status}, Duration: ${requestDuration}ms`);
    console.log(`Active VUs: ${__VU}`);
  }

  // Variable sleep based on phase
  let sleepTime;
  switch (currentPhase) {
    case 'SPIKE_RISE':
    case 'SPIKE_HOLD':
    case 'SPIKE_HIGH':
      sleepTime = Math.random() * 0.5 + 0.1; // 0.1-0.6s (fast during spike)
      break;
    case 'RECOVERY':
      sleepTime = Math.random() * 1 + 0.5; // 0.5-1.5s (normal during recovery)
      break;
    default:
      sleepTime = Math.random() * 2 + 1; // 1-3s (normal baseline)
  }

  sleep(sleepTime);
}

export function handleSummary(data) {
  const totalRequests = data.metrics.http_reqs.values.count;
  const successRate = spikeHandlingRate.count > 0 ? (spikeHandlingRate.count / totalRequests * 100) : 0;
  const recoveryRate = spikeRecoveryRate.count > 0 ? (spikeRecoveryRate.count / spikeRecoveryRate.count * 100) : 0;

  // Analyze phase performance
  const phaseAnalysis = Object.entries(phaseMetrics).map(([phase, metrics]) => {
    const successRatePhase = metrics.requests > 0 ? (metrics.successes / metrics.requests * 100) : 0;
    const avgDuration = metrics.requests > 0 ? (metrics.totalDuration / metrics.requests) : 0;
    const errorRatePhase = metrics.requests > 0 ? (metrics.errors / metrics.requests * 100) : 0;

    return {
      phase: SPIKE_PHASES[phase]?.name || phase,
      requests: metrics.requests,
      successRate: successRatePhase.toFixed(1),
      avgResponseTime: avgDuration.toFixed(0),
      errorRate: errorRatePhase.toFixed(1),
    };
  });

  // Calculate spike handling efficiency
  const baselinePhase = phaseMetrics.BASELINE_1;
  const spikePhase = phaseMetrics.SPIKE_HOLD;
  const recoveryPhase = phaseMetrics.RECOVERY;

  const baselineSuccessRate = baselinePhase.requests > 0 ? (baselinePhase.successes / baselinePhase.requests) : 0;
  const spikeSuccessRate = spikePhase.requests > 0 ? (spikePhase.successes / spikePhase.requests) : 0;
  const recoverySuccessRate = recoveryPhase.requests > 0 ? (recoveryPhase.successes / recoveryPhase.requests) : 0;

  const spikeDegradation = baselineSuccessRate > 0 ? ((baselineSuccessRate - spikeSuccessRate) / baselineSuccessRate * 100) : 0;
  const recoveryEfficiency = baselineSuccessRate > 0 ? (recoverySuccessRate / baselineSuccessRate * 100) : 0;

  return {
    'spike-test-results.json': JSON.stringify(data, null, 2),
    'spike-phase-analysis.json': JSON.stringify(phaseAnalysis, null, 2),
    stdout: `
=== Spike Test Results Summary ===

Overall Performance:
  Total Requests: ${totalRequests}
  Overall Success Rate: ${successRate.toFixed(2)}%
  Recovery Success Rate: ${recoveryRate.toFixed(2)}%
  Spike Errors: ${spikeErrors.count}

Response Times:
  Min: ${spikeResponseTime.min ? spikeResponseTime.min.toFixed(2) : 'N/A'}ms
  Max: ${spikeResponseTime.max ? spikeResponseTime.max.toFixed(2) : 'N/A'}ms
  Average: ${spikeResponseTime.avg ? spikeResponseTime.avg.toFixed(2) : 'N/A'}ms
  90th: ${spikeResponseTime.p(90) ? spikeResponseTime.p(90).toFixed(2) : 'N/A'}ms
  95th: ${spikeResponseTime.p(95) ? spikeResponseTime.p(95).toFixed(2) : 'N/A'}ms

Phase-by-Phase Analysis:
${phaseAnalysis.map(p => `
  ${p.phase}:
    Requests: ${p.requests}
    Success Rate: ${p.successRate}%
    Avg Response: ${p.avgResponseTime}ms
    Error Rate: ${p.errorRate}%`).join('')}

Spike Handling Assessment:
  Baseline Success Rate: ${(baselineSuccessRate * 100).toFixed(1)}%
  Spike Success Rate: ${(spikeSuccessRate * 100).toFixed(1)}%
  Recovery Success Rate: ${(recoverySuccessRate * 100).toFixed(1)}%

  Spike Degradation: ${spikeDegradation.toFixed(1)}%
  Recovery Efficiency: ${recoveryEfficiency.toFixed(1)}%

System Resilience:
✅ Handled spike without crashing: ${spikeErrors.count < 50 ? 'PASS' : 'FAIL'}
✅ Recovery within acceptable range: ${recoveryEfficiency >= 80 ? 'PASS' : 'FAIL'}
✅ Spike degradation acceptable: ${spikeDegradation <= 30 ? 'PASS' : 'FAIL'}
✅ Response times consistent: ${spikeResponseTime.p(95) && spikeResponseTime.p(95) < 1500 ? 'PASS' : 'FAIL'}

Spike Test Grade:
A: Excellent (≥90% success, ≤20% degradation, ≥90% recovery)
B: Good (≥80% success, ≤30% degradation, ≥80% recovery)
C: Acceptable (≥70% success, ≤40% degradation, ≥70% recovery)
D: Poor (<70% success or >40% degradation)

Overall Spike Grade: ${
  successRate >= 90 && spikeDegradation <= 20 && recoveryEfficiency >= 90 ? 'A' :
  successRate >= 80 && spikeDegradation <= 30 && recoveryEfficiency >= 80 ? 'B' :
  successRate >= 70 && spikeDegradation <= 40 && recoveryEfficiency >= 70 ? 'C' : 'D'
}

Recommendations:
${spikeDegradation > 30 ? '- Consider implementing better caching during high load\n' : ''}
${recoveryEfficiency < 80 ? '- Improve recovery mechanisms after traffic spikes\n' : ''}
${spikeErrors.count >= 50 ? '- Address system stability issues during high load\n' : ''}
${spikeResponseTime.p(95) && spikeResponseTime.p(95) > 1500 ? '- Optimize response times under load\n' : ''}
    `,
  };
}