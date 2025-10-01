package hris.hris.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateLimitingService {

    private final Cache<String, LoginAttemptInfo> loginAttemptsCache;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    public RateLimitingService() {
        this.loginAttemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();
    }

    public boolean isRateLimited(String email) {
        LoginAttemptInfo attemptInfo = loginAttemptsCache.getIfPresent(email);

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
        LoginAttemptInfo attemptInfo = loginAttemptsCache.get(email, k -> new LoginAttemptInfo());
        attemptInfo.incrementFailedAttempts();

        if (attemptInfo.getFailedAttempts() >= MAX_ATTEMPTS) {
            attemptInfo.setLocked(true);
            attemptInfo.setLockEndTime(LocalDateTime.now().plus(LOCK_DURATION));
            log.warn("User {} has been locked due to too many failed attempts until {}",
                    email, attemptInfo.getLockEndTime());
        }

        loginAttemptsCache.put(email, attemptInfo);
    }

    public void recordSuccessfulLogin(String email) {
        LoginAttemptInfo attemptInfo = loginAttemptsCache.getIfPresent(email);
        if (attemptInfo != null) {
            attemptInfo.reset();
            loginAttemptsCache.put(email, attemptInfo);
        }
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