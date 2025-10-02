# HRIS API Load Testing with k6

This directory contains comprehensive load tests for the HRIS (Human Resource Information System) API endpoints using k6.

## 📊 Test Scenarios

### 1. Authentication Load Test (`auth-load-test.js`)
- **Endpoint**: `/api/auth/login`, `/api/auth/validate`, `/api/auth/me`
- **Users**: Up to 50 concurrent users
- **Duration**: ~10 minutes
- **SLA**: 95% of requests under 500ms, <5% error rate

### 2. Attendance Load Test (`attendance-load-test.js`)
- **Endpoints**: Clock in/out, today's attendance, attendance status, history
- **Users**: Up to 30 concurrent users
- **Duration**: ~7 minutes
- **SLA**: 95% of requests under 1s, <10% error rate

### 3. Leave Management Load Test (`leave-load-test.js`)
- **Endpoints**: Leave requests, approval/rejection, current leave
- **Users**: Up to 15 concurrent users
- **Duration**: ~9 minutes
- **SLA**: 95% of requests under 2s, <10% error rate

### 4. Business Travel Load Test (`business-travel-load-test.js`)
- **Endpoints**: Travel requests, approval/rejection, my requests
- **Users**: Up to 10 concurrent users
- **Duration**: ~7 minutes
- **SLA**: 95% of requests under 3s, <15% error rate

### 5. HR Admin Load Test (`admin-load-test.js`)
- **Endpoints**: Employee management, registration, updates, face templates
- **Users**: Up to 5 concurrent admin users
- **Duration**: ~7 minutes
- **SLA**: 95% of requests under 5s, <20% error rate

### 6. Comprehensive Load Test (`../k6-load-test.js`)
- **All endpoints**: Simulates real-world usage patterns
- **Users**: Up to 100 concurrent users
- **Duration**: ~20 minutes
- **SLA**: 95% of requests under 2s, <10% error rate

## 🚀 Running the Tests

### Prerequisites
- k6 installed: `npm install -g k6`
- HRIS backend running on `http://localhost:8080`
- Database and Redis running via Docker Compose

### Individual Test Execution

```bash
# Run authentication load test
k6 run k6-tests/auth-load-test.js

# Run attendance load test
k6 run k6-tests/attendance-load-test.js

# Run leave management load test
k6 run k6-tests/leave-load-test.js

# Run business travel load test
k6 run k6-tests/business-travel-load-test.js

# Run HR admin load test
k6 run k6-tests/admin-load-test.js

# Run comprehensive load test
k6 run k6-load-test.js
```

### Running Tests with Output

```bash
# Run test with HTML output
k6 run k6-tests/auth-load-test.js --out html=report.html

# Run test with JSON output
k6 run k6-tests/auth-load-test.js --out json=report.json

# Run test with InfluxDB output for dashboard
k6 run k6-tests/auth-load-test.js --out influxdb=http://localhost:8086/k6
```

## 📈 Test Configuration

### Environment Variables

Set environment variables for different test scenarios:

```bash
# For staging environment
BASE_URL=https://staging.hris.com k6 run k6-load-test.js

# For production (use with caution)
BASE_URL=https://api.hris.com k6 run k6-load-test.js

# For local development
BASE_URL=http://localhost:8080 k6 run k6-load-test.js
```

### Custom Load Profiles

Modify the `options` object in each test file to customize load profiles:

```javascript
export const options = {
  stages: [
    { duration: '2m', target: 10 },  // Ramp up
    { duration: '5m', target: 10 },  // Sustain
    { duration: '1m', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.1'],
  },
};
```

## 📊 Monitoring and Analysis

### Key Metrics Tracked

1. **Response Times**: p50, p90, p95 percentiles
2. **Error Rates**: HTTP error rates and custom error metrics
3. **Throughput**: Requests per second
4. **Virtual Users**: Active concurrent users
5. **API Endpoints**: Individual endpoint performance

### k6 Cloud Integration

```bash
# Upload and run test on k6 Cloud
k6 login --token YOUR_K6_CLOUD_TOKEN
k6 cloud k6-load-test.js
```

### Grafana Dashboard Setup

Use this InfluxDB query for response time monitoring:

```sql
SELECT mean("value")
FROM "http_req_duration"
WHERE $timeFilter
GROUP BY time($__interval), "url" fill(null)
```

## 🔧 Test Data Management

### Authentication
- Test users: admin@hris.com, john.doe@company.com, etc.
- Passwords: configured in test files
- JWT tokens: automatically managed

### Attendance Testing
- GPS coordinates: simulated around office location
- Face images: base64 encoded dummy data
- Time-based constraints: handled gracefully

### Leave/Travel Testing
- Various leave types: ANNUAL, SICK, MATERNITY, etc.
- Different destinations and purposes
- Approval workflow simulation

### Admin Operations
- Employee registration with unique emails
- Face template management
- Bulk operations simulation

## 🚨 Important Notes

### Test Environment Safety
- **NEVER** run load tests against production without proper authorization
- **ALWAYS** use test databases and environments
- **BACKUP** production data before any testing

### Rate Limiting
- The API has rate limiting (5 attempts per 5 minutes)
- Tests are designed to work within these limits
- Adjust test parameters if rate limiting affects results

### Data Cleanup
- Load tests create test data
- Implement cleanup scripts after test runs
- Monitor database growth during testing

## 📝 Test Reports

### Generating Reports

```bash
# Generate HTML report
k6 run k6-load-test.js --out html=report.html

# Generate JSON report
k6 run k6-load-test.js --out json=report.json

# Generate JUnit XML report
k6 run k6-load-test.js --out junit=junit.xml
```

### Analyzing Results

1. **Pass/Fail Criteria**: Check if SLAs were met
2. **Bottlenecks**: Identify slow endpoints
3. **Error Patterns**: Analyze failure rates
4. **Resource Usage**: Monitor server resources during tests

## 🔧 Troubleshooting

### Common Issues

1. **Connection Refused**: Ensure HRIS backend is running
2. **Authentication Errors**: Check test user credentials
3. **Rate Limiting**: Reduce concurrent users or increase time between requests
4. **Database Issues**: Verify database connectivity and migrations

### Debug Mode

Run tests with debug output:

```bash
DEBUG=http k6 run k6-load-test.js
```

## 🔄 Continuous Integration

### GitHub Actions Example

```yaml
name: Load Testing
on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM

jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup k6
        uses: grafana/k6-action@v0.2.0
      - name: Run load tests
        run: k6 run k6-load-test.js --out json=results.json
      - name: Upload results
        uses: actions/upload-artifact@v3
        with:
          name: load-test-results
          path: results.json
```

---

For more information about k6, visit: https://k6.io/docs/