# 🔐 Rate Limiting Best Practices Guide

## 📊 Current Rate Limiting Configuration

### Login Rate Limiting
- **Max Attempts**: 5 failed attempts
- **Lock Duration**: 5 minutes
- **Storage**: Redis (for distributed systems)
- **Auto Reset**: After successful login

### Password Reset Rate Limiting
- **Request Limit**: 3 requests per hour per email
- **Confirmation Limit**: 5 confirmations per hour
- **Rate Limiter**: Resilience4j
- **Retry Attempts**: 3 with 1s delay

---

## 🛠 Best Practices When Rate Limited

### 1. **Understand the Rate Limit Response**
```json
{
  "data": null,
  "message": "Rate limit exceeded. Please try again later."
}
```
- **HTTP Status**: 429 (Too Many Requests)
- **Meaning**: You've exceeded allowed attempts
- **Solution**: Wait and try again later

### 2. **Immediate Actions When Rate Limited**

#### ✅ DO:
- **Wait for 5 minutes** (for login rate limit)
- **Check your credentials** before retrying
- **Clear your browser cache** if needed
- **Use correct email and password** combination

#### ❌ DON'T:
- **Spam the login endpoint** - this extends the lock time
- **Try multiple passwords quickly** - triggers faster lockout
- **Use automated scripts** - will be blocked immediately
- **Try to bypass the system** - security logs will record attempts

### 3. **Development & Testing Best Practices**

#### During Development:
```bash
# Use different test accounts to avoid rate limiting
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test1@hris.com", "password": "test123"}'

# Reset rate limit manually (development only)
redis-cli FLUSHALL

# Or use the debug endpoint
curl -X POST http://localhost:8081/api/auth/debug/reset-rate-limit \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

#### Rate Limit Reset Commands:
```bash
# Clear specific user rate limit
redis-cli DEL "rate_limit:your-email@example.com"

# Clear all rate limits (development only!)
redis-cli FLUSHDB
```

### 4. **Frontend Implementation Best Practices**

#### React/Vue.js Example:
```javascript
class AuthService {
  async login(email, password) {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      if (response.status === 429) {
        const remainingTime = this.parseRetryAfter(response);
        throw new Error(`Too many attempts. Please try again in ${remainingTime} minutes.`);
      }

      return await response.json();
    } catch (error) {
      this.handleRateLimit(error);
      throw error;
    }
  }

  handleRateLimit(error) {
    if (error.message.includes('Too many attempts')) {
      // Show countdown timer
      this.showRateLimitModal(error.message);
      // Disable login button temporarily
      this.disableLoginForm(300000); // 5 minutes
    }
  }
}
```

### 5. **Error Handling & User Experience**

#### User-Friendly Messages:
```javascript
const rateLimitMessages = {
  429: {
    title: "Too Many Login Attempts",
    message: "For your security, we've temporarily locked your account due to multiple failed login attempts.",
    subMessage: "Please wait 5 minutes before trying again, or reset your password if you've forgotten it.",
    actions: [
      { text: "Reset Password", action: "resetPassword" },
      { text: "Wait and Retry", action: "retry" }
    ]
  }
};
```

#### Progressive Lockout Strategy:
```javascript
const lockoutStrategy = {
  1: { attempts: 3, delay: 1 },     // 1 minute after 3 attempts
  2: { attempts: 5, delay: 5 },     // 5 minutes after 5 attempts
  3: { attempts: 10, delay: 15 },   // 15 minutes after 10 attempts
  4: { attempts: 15, delay: 60 }    // 1 hour after 15 attempts
};
```

---

## 🔧 Configuration Examples

### Custom Rate Limiting (application.properties)
```properties
# Login Rate Limiting
login.rate.limit.max-attempts=5
login.rate.limit.lock-duration-minutes=5

# Password Reset Rate Limiting
resilience4j.ratelimiter.instances.password-reset-request.limit-for-period=3
resilience4j.ratelimiter.instances.password-reset-request.limit-refresh-period=1h
resilience4j.ratelimiter.instances.password-reset-request.timeout-duration=5s

# API Rate Limiting (if needed)
api.rate.limit.requests-per-minute=60
api.rate.limit.requests-per-hour=1000
```

### Environment-Specific Settings
```properties
# Development (less restrictive)
spring.profiles.active=dev
login.rate.limit.max-attempts=10
login.rate.limit.lock-duration-minutes=1

# Production (more restrictive)
spring.profiles.active=prod
login.rate.limit.max-attempts=5
login.rate.limit.lock-duration-minutes=15
```

---

## 📈 Monitoring & Analytics

### Rate Limit Metrics to Track:
```bash
# Monitor Redis rate limit keys
redis-cli KEYS "rate_limit:*" | wc -l

# Check specific user rate limit
redis-cli GET "rate_limit:user@example.com"

# Monitor failed login attempts
grep "Rate limit exceeded" /var/log/hris/application.log
```

### Dashboard Metrics:
- **Total Rate Limited Requests**: Track overall rate limit hits
- **Rate Limited Users**: Number of unique users rate limited
- **Average Lockout Time**: How long users wait
- **Successful vs Failed Logins**: Ratio analysis
- **Peak Attack Times**: When brute force attempts occur

---

## 🚨 Security Considerations

### 1. **Log All Rate Limit Events**
```java
log.warn("Rate limit triggered for IP: {}, Email: {}, User-Agent: {}",
    getClientIP(), email, getUserAgent());
```

### 2. **Implement Progressive Delays**
```java
// Exponential backoff for repeated offenses
long delayMinutes = (long) Math.pow(2, failedAttempts);
lockEndTime = LocalDateTime.now().plusMinutes(delayMinutes);
```

### 3. **IP-Based Rate Limiting**
```java
// Add IP-based limiting in addition to email-based
String ipKey = "ip_rate_limit:" + clientIP;
if (isIPRateLimited(clientIP)) {
    return ResponseEntity.status(429).body("IP blocked");
}
```

### 4. **CAPTCHA Integration**
```java
// After 3 failed attempts, require CAPTCHA
if (failedAttempts >= 3 && !captchaValid) {
    return ResponseEntity.badRequest().body("CAPTCHA required");
}
```

---

## 🛠 Troubleshooting Guide

### Common Issues & Solutions:

#### 1. **Legitimate Users Rate Limited**
```bash
# Check if user is actually rate limited
redis-cli GET "rate_limit:user@example.com"

# Clear the rate limit if needed
redis-cli DEL "rate_limit:user@example.com"
```

#### 2. **Redis Connection Issues**
```bash
# Check Redis connection
redis-cli ping

# Check Redis logs
docker logs redis-container
```

#### 3. **Rate Limit Not Working**
```properties
# Verify configuration
spring.redis.host=localhost
spring.redis.port=6379

# Check rate limiting service logs
logging.level.hris.hris.service.RateLimitingService=DEBUG
```

---

## 📝 Testing Rate Limiting

### Automated Test Cases:
```java
@Test
public void testRateLimiting() {
    // Make 5 failed login attempts
    for (int i = 0; i < 5; i++) {
        ResponseEntity<?> response = login("test@example.com", "wrongpass");
        assertEquals(401, response.getStatusCodeValue());
    }

    // 6th attempt should be rate limited
    ResponseEntity<?> response = login("test@example.com", "wrongpass");
    assertEquals(429, response.getStatusCodeValue());

    // Successful login should reset rate limit
    Thread.sleep(1000); // Wait for Redis
    ResponseEntity<?> successResponse = login("test@example.com", "correctpass");
    assertEquals(200, successResponse.getStatusCodeValue());
}
```

### Load Testing Script:
```bash
#!/bin/bash
# Test rate limiting with multiple concurrent requests

for i in {1..10}; do
  curl -X POST http://localhost:8081/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email": "test@example.com", "password": "wrong"}' &
done

wait
echo "All requests completed"
```

---

## 🔄 Reset Rate Limits (Development Only)

### Manual Reset Commands:
```bash
# Reset specific user
redis-cli DEL "rate_limit:admin@hris.com"

# Reset all users (dangerous in production!)
redis-cli --scan --pattern "rate_limit:*" | xargs redis-cli DEL

# Check remaining TTL
redis-cli TTL "rate_limit:user@example.com"
```

### Admin Reset Endpoint (Optional):
```java
@PostMapping("/admin/reset-rate-limit")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> resetRateLimit(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    rateLimitingService.resetRateLimit(email);
    return ResponseEntity.ok("Rate limit reset for: " + email);
}
```

---

## 📚 Additional Resources

### Documentation:
- [Spring Security Rate Limiting](https://spring.io/guides/gs/securing-web/)
- [Redis Rate Limiting Patterns](https://redis.io/docs/data-types/probabilistic/)
- [Resilience4j Rate Limiter](https://resilience4j.readme.io/docs/ratelimiter)

### Tools:
- [Redis Desktop Manager](https://redisdesktop.com/)
- [Postman Collections for API Testing](https://www.postman.com/)
- [Apache JMeter for Load Testing](https://jmeter.apache.org/)

---

**Remember**: Rate limiting is a security feature, not a bug! 🛡️

For support, check the application logs or contact the development team.