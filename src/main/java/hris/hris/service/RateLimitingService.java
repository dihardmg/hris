package hris.hris.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateLimitingService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${rate.limit.login.failed.max-attempts}")
    private int maxFailedAttempts;

    @Value("${rate.limit.login.failed.window-duration}")
    private long failedLoginWindowDuration;

    @Value("${rate.limit.login.success.per-account}")
    private int maxSuccessLoginPerAccount;

    @Value("${rate.limit.login.success.per-ip}")
    private int maxSuccessLoginPerIP;

    @Value("${rate.limit.login.success.window-duration}")
    private long successLoginWindowDuration;

    private final ObjectMapper objectMapper;

    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String SUCCESS_LOGIN_PREFIX = "login_success:";
    private static final String SUCCESS_LOGIN_IP_PREFIX = "login_success_ip:";
    private static final Duration CACHE_EXPIRY = Duration.ofMinutes(10);

    public RateLimitingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public boolean isRateLimited(String email) {
        String key = getRateLimitKey(email);
        LoginAttemptInfo attemptInfo = getLoginAttemptInfo(key);

        if (attemptInfo == null) {
            return false;
        }

        if (attemptInfo.isLocked() && attemptInfo.getLockEndTime().isAfter(LocalDateTime.now())) {
            log.info("User {} is still locked until {}", email, attemptInfo.getLockEndTime());
            return true;
        }

        return false;
    }

    public void recordFailedLogin(String email) {
        String key = getRateLimitKey(email);
        LoginAttemptInfo attemptInfo = getLoginAttemptInfo(key);

        if (attemptInfo == null) {
            attemptInfo = new LoginAttemptInfo();
        }

        attemptInfo.incrementFailedAttempts();

        if (attemptInfo.getFailedAttempts() >= maxFailedAttempts) {
            attemptInfo.setLocked(true);
            attemptInfo.setLockEndTime(LocalDateTime.now().plus(LOCK_DURATION));
            log.warn("User {} has been locked due to too many failed attempts until {}",
                    email, attemptInfo.getLockEndTime());
        }

        try {
            String json = objectMapper.writeValueAsString(attemptInfo);
            redisTemplate.opsForValue().set(key, json, CACHE_EXPIRY.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error storing rate limit info for user {}: {}", email, e.getMessage());
        }
    }

    public void recordSuccessfulLogin(String email) {
        String key = getRateLimitKey(email);
        try {
            redisTemplate.delete(key);
            log.info("Rate limit counter reset for user {} due to successful login", email);
        } catch (Exception e) {
            log.error("Error resetting rate limit for user {}: {}", email, e.getMessage());
        }
    }

    public void resetRateLimit(String email) {
        String key = getRateLimitKey(email);
        try {
            redisTemplate.delete(key);
            log.info("Rate limit manually reset for user {}", email);
        } catch (Exception e) {
            log.error("Error manually resetting rate limit for user {}: {}", email, e.getMessage());
        }
    }

    public boolean isUserRateLimited(String email) {
        return isRateLimited(email);
    }

    public long getRateLimitTTL(String email) {
        String key = getRateLimitKey(email);
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting TTL for rate limit key {}: {}", key, e.getMessage());
            return -1;
        }
    }

    private String getRateLimitKey(String email) {
        return RATE_LIMIT_PREFIX + email;
    }

    private LoginAttemptInfo getLoginAttemptInfo(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                String json = value.toString();
                return objectMapper.readValue(json, LoginAttemptInfo.class);
            }
        } catch (Exception e) {
            log.error("Error retrieving rate limit info for key {}: {}", key, e.getMessage());
        }
        return null;
    }

    // Success Login Rate Limiting Methods

    /**
     * Check if account has exceeded success login rate limit
     */
    public boolean isLoginSuccessRateLimited(String email) {
        String key = getSuccessLoginKey(email);
        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount == 1) {
                // First time setting this key - set expiry
                redisTemplate.expire(key, successLoginWindowDuration, TimeUnit.MILLISECONDS);
            }

            boolean isRateLimited = currentCount > maxSuccessLoginPerAccount;
            if (isRateLimited) {
                log.warn("Account {} has exceeded success login rate limit: {}/{}",
                    email, currentCount, maxSuccessLoginPerAccount);
            }

            return isRateLimited;
        } catch (Exception e) {
            log.error("Error checking success login rate limit for email {}: {}", email, e.getMessage());
            return false; // Fail open - don't block on errors
        }
    }

    /**
     * Check if IP has exceeded success login rate limit
     */
    public boolean isLoginSuccessRateLimitedByIP(String clientIP) {
        String key = getSuccessLoginIPKey(clientIP);
        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount == 1) {
                // First time setting this key - set expiry
                redisTemplate.expire(key, successLoginWindowDuration, TimeUnit.MILLISECONDS);
            }

            boolean isRateLimited = currentCount > maxSuccessLoginPerIP;
            if (isRateLimited) {
                log.warn("IP {} has exceeded success login rate limit: {}/{}",
                    clientIP, currentCount, maxSuccessLoginPerIP);
            }

            return isRateLimited;
        } catch (Exception e) {
            log.error("Error checking success login rate limit for IP {}: {}", clientIP, e.getMessage());
            return false; // Fail open - don't block on errors
        }
    }

    /**
     * Record successful login for both account and IP tracking
     */
    public void recordSuccessfulLogin(String email, String clientIP) {
        // Record successful login for account
        String accountKey = getSuccessLoginKey(email);
        try {
            redisTemplate.opsForValue().increment(accountKey);
            redisTemplate.expire(accountKey, successLoginWindowDuration, TimeUnit.MILLISECONDS);
            log.debug("Recorded successful login for account {}", email);
        } catch (Exception e) {
            log.error("Error recording successful login for account {}: {}", email, e.getMessage());
        }

        // Record successful login for IP
        String ipKey = getSuccessLoginIPKey(clientIP);
        try {
            redisTemplate.opsForValue().increment(ipKey);
            redisTemplate.expire(ipKey, successLoginWindowDuration, TimeUnit.MILLISECONDS);
            log.debug("Recorded successful login for IP {}", clientIP);
        } catch (Exception e) {
            log.error("Error recording successful login for IP {}: {}", clientIP, e.getMessage());
        }
    }

    /**
     * Get TTL for account success login rate limit
     */
    public long getLoginSuccessRateLimitTTL(String email) {
        String key = getSuccessLoginKey(email);
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting TTL for success login rate limit key {}: {}", key, e.getMessage());
            return -1;
        }
    }

    /**
     * Get TTL for IP success login rate limit
     */
    public long getLoginSuccessRateLimitTTLByIP(String clientIP) {
        String key = getSuccessLoginIPKey(clientIP);
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error getting TTL for success login rate limit IP key {}: {}", key, e.getMessage());
            return -1;
        }
    }

    /**
     * Reset success login rate limit for account (for testing/admin purposes)
     */
    public void resetSuccessLoginRateLimit(String email) {
        String key = getSuccessLoginKey(email);
        try {
            redisTemplate.delete(key);
            log.info("Success login rate limit reset for account {}", email);
        } catch (Exception e) {
            log.error("Error resetting success login rate limit for account {}: {}", email, e.getMessage());
        }
    }

    /**
     * Reset success login rate limit for IP (for testing/admin purposes)
     */
    public void resetSuccessLoginRateLimitByIP(String clientIP) {
        String key = getSuccessLoginIPKey(clientIP);
        try {
            redisTemplate.delete(key);
            log.info("Success login rate limit reset for IP {}", clientIP);
        } catch (Exception e) {
            log.error("Error resetting success login rate limit for IP {}: {}", clientIP, e.getMessage());
        }
    }

    private String getSuccessLoginKey(String email) {
        return SUCCESS_LOGIN_PREFIX + email;
    }

    private String getSuccessLoginIPKey(String clientIP) {
        return SUCCESS_LOGIN_IP_PREFIX + clientIP;
    }

    private static class LoginAttemptInfo {
        private int failedAttempts = 0;
        private boolean locked = false;
        private LocalDateTime lockEndTime;

        public int getFailedAttempts() {
            return failedAttempts;
        }

        public void incrementFailedAttempts() {
            this.failedAttempts++;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public LocalDateTime getLockEndTime() {
            return lockEndTime;
        }

        public void setLockEndTime(LocalDateTime lockEndTime) {
            this.lockEndTime = lockEndTime;
        }

        public void reset() {
            this.failedAttempts = 0;
            this.locked = false;
            this.lockEndTime = null;
        }
    }
}