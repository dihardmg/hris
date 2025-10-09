# K6 Load Testing for HRIS Login Endpoint

This repository contains comprehensive load testing scripts for the HRIS login API endpoint (`/api/auth/login`) using k6, following performance testing best practices.

## 📋 Test Scenarios Overview

| Test Type | Purpose | Duration | VUs | Focus Area |
|-----------|---------|----------|-----|------------|
| **Basic Load Test** | Normal usage patterns | 2 minutes | 10 | Baseline performance |
| **Rate Limiting Test** | Verify rate limiting behavior | 6 minutes | 1-25 | Rate limit effectiveness |
| **Stress Test** | Find breaking point | 5 minutes | 50-300 | System capacity |
| **Soak Test** | Endurance and memory leaks | 10 minutes | 20 | Long-term stability |
| **Spike Test** | Traffic surge handling | 2 minutes | 5-150 | Recovery capability |

### 🎯 Specific Tests for mcrdik@gmail.com

| Test Type | Purpose | Duration | VUs | Focus Area |
|-----------|---------|----------|-----|------------|
| **Quick Test** | Fast validation | 30 seconds | 5 | Basic functionality |
| **Comprehensive Test** | Detailed analysis | 2 minutes | 5-15 | Performance metrics |
| **Rate Limiting Test** | User-specific rate limiting | 6 minutes | 1 | Rate limit verification |

**Request Format:**
```bash
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "mcrdik@gmail.com",
  "password": "week123"
}
```

## 🏗 Architecture

```
k6/
├── config/
│   └── config.js              # Test configuration and environments
├── data/
│   └── test-users.json        # Test user credentials
├── tests/
│   ├── basic-load-test.js     # Basic load testing
│   ├── rate-limiting-test.js  # Rate limiting verification
│   ├── stress-test.js         # Stress testing
│   ├── soak-test.js          # Endurance testing
│   └── spike-test.js         # Traffic spike testing
├── docs/
│   └── usage-guide.md        # Detailed usage instructions
└── README.md                  # This file
```

## 🚀 Quick Start

### Prerequisites

1. **Install k6:**
   ```bash
   # macOS
   brew install k6

   # Linux
   sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
   echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
   sudo apt-get update
   sudo apt-get install k6

   # Windows (using Chocolatey)
   choco install k6
   ```

2. **HRIS Application Running:**
   - Ensure HRIS application is running on `http://localhost:8081`
   - Test users are created in the system

### 🎯 Quick Tests for mcrdik@gmail.com

**For immediate testing with the specific user credentials:**

#### 1. Quick Basic Test (30 seconds)
```bash
# Simple and fast
./test-mcrdik.sh quick

# With debug output
./test-mcrdik.sh -d quick

# Against different server
./test-mcrdik.sh -u https://api.hris.com quick
```

#### 2. Comprehensive Test (2 minutes)
```bash
# Full-featured test with detailed metrics
./test-mcrdik.sh basic

# Debug mode for detailed logging
./test-mcrdik.sh -d basic
```

#### 3. Rate Limiting Test (6 minutes)
```bash
# Test rate limiting behavior for this specific user
./test-mcrdik.sh rate-limit

# Monitor rate limiting in detail
./test-mcrdik.sh -d rate-limit
```

**Direct k6 commands:**
```bash
# Quick test
k6 run tests/quick-login-test.js

# Comprehensive test
k6 run tests/mcrdik-login-test.js

# Rate limiting test
k6 run tests/mcrdik-rate-limit-test.js
```

### 🔄 Full Test Suite

#### 1. Basic Load Test
```bash
# Normal load testing
k6 run tests/basic-load-test.js

# With debug output
DEBUG=true k6 run tests/basic-load-test.js

# Different environment
ENV=staging k6 run tests/basic-load-test.js
```

#### 2. Rate Limiting Test
```bash
# Test rate limiting behavior
k6 run tests/rate-limiting-test.js

# Monitor rate limiting in real-time
DEBUG=true k6 run tests/rate-limiting-test.js
```

#### 3. Stress Test
```bash
# Find system breaking point
k6 run tests/stress-test.js

# Generate detailed report
k6 run tests/stress-test.js --out json=stress-results.json
```

#### 4. Soak Test (Endurance)
```bash
# 10-minute endurance test
k6 run tests/soak-test.js

# Monitor for memory leaks
DEBUG=true k6 run tests/soak-test.js
```

#### 5. Spike Test
```bash
# Test traffic surge handling
k6 run tests/spike-test.js

# Analyze recovery patterns
k6 run tests/spike-test.js --out json=spike-results.json
```

## 📊 Expected Results Based on Rate Limiting

### Rate Limiting Configuration (from README.md):
- **Failed Login**: 5 attempts per 5 minutes per email/IP
- **Success Login**: 5 per account, 20 per IP per 5 minutes
- **Password Reset**: 3 requests per hour per email

### Expected Test Behaviors:

#### Rate Limiting Test:
- **Account-based**: After 5 successful logins, should see 429 responses
- **IP-based**: After 20 successful logins from same IP, should see 429 responses
- **Recovery**: Rate limits should reset after 5 minutes

#### Stress Test:
- **Gradual degradation** as VUs increase
- **Rate limiting activation** under high load
- **System stability** even with high concurrency

#### Spike Test:
- **Temporary degradation** during spikes
- **Quick recovery** when load returns to normal
- **Rate limiting protection** against abuse

## 📈 Performance Targets

| Metric | Target | Rationale |
|--------|--------|-----------|
| **Response Time (p95)** | < 500ms | Good user experience |
| **Success Rate** | > 95% | Acceptable error rate |
| **Rate Limiting** | Active | Security requirement |
| **System Stability** | No crashes | Reliability requirement |
| **Memory Usage** | Stable | No memory leaks |

## 🔧 Configuration

### Environment Variables

```bash
# Base URL for the API
BASE_URL=http://localhost:8081

# Enable debug logging
DEBUG=true

# Environment (development/staging/production)
ENV=development
```

### Customizing Test Data

Edit `data/test-users.json` to add more test users:

```json
[
  {
    "email": "test.user@company.com",
    "password": "testpass123",
    "description": "Additional test user"
  }
]
```

### Modifying Test Scenarios

Edit `config/config.js` to adjust test parameters:

```javascript
export const scenarios = {
  basic_load: {
    executor: 'ramping-vus',
    stages: [
      { duration: '30s', target: 20 },  // Increase VUs
      { duration: '1m', target: 20 },
      { duration: '30s', target: 0 },
    ],
  },
};
```

## 📋 Test Execution Checklist

### Before Running Tests:

- [ ] HRIS application is running and accessible
- [ ] Test users exist in the database
- [ ] Redis is running (for rate limiting)
- [ ] Sufficient system resources available
- [ ] No other performance tests running

### During Tests:

- [ ] Monitor application logs
- [ ] Watch system resources (CPU, Memory)
- [ ] Check Redis usage
- [ ] Monitor network connectivity

### After Tests:

- [ ] Review test results
- [ ] Check for any errors or warnings
- [ ] Verify rate limiting behavior
- [ ] Document any performance issues

## 🐛 Troubleshooting

### Common Issues:

#### 1. Connection Refused
```bash
Error: connection refused
```
**Solution:** Ensure HRIS application is running on the correct port.

#### 2. High Failure Rate
```bash
Error rate > 50%
```
**Solutions:**
- Check application health
- Verify test user credentials
- Check rate limiting configuration

#### 3. Rate Limiting Not Working
```bash
No 429 responses observed
```
**Solutions:**
- Verify Redis is running
- Check rate limiting configuration
- Ensure test users are valid

#### 4. Memory Issues
```bash
Out of memory errors
```
**Solutions:**
- Reduce number of VUs
- Shorten test duration
- Monitor system resources

### Debug Mode

Run tests with debug mode for detailed logging:

```bash
DEBUG=true k6 run tests/basic-load-test.js
```

This will provide:
- Individual request details
- Response body previews
- Error stack traces
- Rate limiting hit information

## 📊 Interpreting Results

### Success Metrics:
- **Success Rate:** Percentage of successful requests (should be >95%)
- **Response Time:** How fast requests are processed
- **Throughput:** Requests per second handled

### Rate Limiting Metrics:
- **Rate Limit Hits:** How often rate limiting is triggered
- **Rate Limit Response Time:** Speed of rate limit responses
- **Recovery Time:** How long until rate limits reset

### System Health Metrics:
- **Error Rate:** Percentage of failed requests
- **System Errors:** Internal server errors (500+)
- **Memory Stability:** Consistent performance over time

### Performance Grades:

- **Grade A:** Excellent (≥98% success, stable performance)
- **Grade B:** Good (≥95% success, minor issues)
- **Grade C:** Acceptable (≥90% success, some degradation)
- **Grade D:** Poor (<90% success or significant issues)

## 🔄 Continuous Integration

### GitHub Actions Example:

```yaml
name: Performance Tests

on:
  schedule:
    - cron: '0 2 * * *'  # Run daily at 2 AM
  workflow_dispatch:

jobs:
  performance-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Setup k6
        run: |
          sudo gpg -k
          sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
          echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
          sudo apt-get update
          sudo apt-get install k6

      - name: Run Basic Load Test
        run: k6 run tests/basic-load-test.js

      - name: Run Rate Limiting Test
        run: k6 run tests/rate-limiting-test.js
```

## 📚 Additional Resources

- [k6 Documentation](https://k6.io/docs/)
- [HRIS API Documentation](../README.md)
- [Rate Limiting Best Practices](https://k6.io/docs/examples/rate-limiting/)
- [Performance Testing Guide](https://k6.io/docs/test-types/load-testing/)

## 🤝 Contributing

When adding new test scenarios:

1. Follow the existing naming conventions
2. Add comprehensive metrics tracking
3. Include proper error handling
4. Document the test purpose and expectations
5. Update this README with new test information

## 📞 Support

For questions about these load tests:
- Check the troubleshooting section above
- Review the k6 documentation
- Examine the HRIS application logs
- Create an issue with detailed error information