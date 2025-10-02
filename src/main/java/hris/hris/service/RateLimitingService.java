package hris.hris.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final ObjectMapper objectMapper;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
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

        if (attemptInfo.getFailedAttempts() >= MAX_ATTEMPTS) {
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