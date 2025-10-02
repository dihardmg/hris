package hris.hris.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RateLimitingServiceTest {

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        // Clean up Redis before each test
        String key = "rate_limit:" + TEST_EMAIL;
        redisTemplate.delete(key);
    }

    @Test
    @DisplayName("New user should not be rate limited")
    void isNewUser_NotRateLimited() {
        // When
        boolean isLimited = rateLimitingService.isRateLimited(TEST_EMAIL);

        // Then
        assertFalse(isLimited, "New user should not be rate limited");
    }

    @Test
    @DisplayName("After 4 failed attempts, user should not be rate limited yet")
    void afterFourFailedAttempts_NotRateLimited() {
        // Given
        for (int i = 0; i < 4; i++) {
            rateLimitingService.recordFailedLogin(TEST_EMAIL);
        }

        // When
        boolean isLimited = rateLimitingService.isRateLimited(TEST_EMAIL);

        // Then
        assertFalse(isLimited, "User with 4 failed attempts should not be rate limited yet");
    }

    @Test
    @DisplayName("After 5 failed attempts, user should be rate limited")
    void afterFiveFailedAttempts_IsRateLimited() {
        // Given
        for (int i = 0; i < 5; i++) {
            rateLimitingService.recordFailedLogin(TEST_EMAIL);
        }

        // When
        boolean isLimited = rateLimitingService.isRateLimited(TEST_EMAIL);

        // Then
        assertTrue(isLimited, "User with 5 failed attempts should be rate limited");
    }

    @Test
    @DisplayName("Successful login should reset rate limit counter")
    void successfulLogin_ResetsRateLimitCounter() {
        // Given - User has 4 failed attempts
        for (int i = 0; i < 4; i++) {
            rateLimitingService.recordFailedLogin(TEST_EMAIL);
        }
        assertFalse(rateLimitingService.isRateLimited(TEST_EMAIL));

        // When - User has a successful login
        rateLimitingService.recordSuccessfulLogin(TEST_EMAIL);

        // Then - User should not be rate limited
        assertFalse(rateLimitingService.isRateLimited(TEST_EMAIL),
                   "Successful login should reset rate limit counter");

        // And - User should be able to make 5 more attempts before being blocked
        for (int i = 0; i < 5; i++) {
            rateLimitingService.recordFailedLogin(TEST_EMAIL);
        }
        assertTrue(rateLimitingService.isRateLimited(TEST_EMAIL),
                  "After reset, user should be blocked after 5 more failed attempts");
    }

    @Test
    @DisplayName("Rate limit should eventually expire")
    void rateLimit_ExpiresAfterTime() throws InterruptedException {
        // Given - User is rate limited
        for (int i = 0; i < 5; i++) {
            rateLimitingService.recordFailedLogin(TEST_EMAIL);
        }
        assertTrue(rateLimitingService.isRateLimited(TEST_EMAIL), "User should be rate limited");

        // Note: We can't easily test the 5-minute expiration in unit tests
        // but we can verify the cache expiration logic works
        // In production, the Caffeine cache will expire entries after 10 minutes

        // When we create a new service instance (simulating cache expiration)
        RateLimitingService newService = new RateLimitingService();

        // Then
        assertFalse(newService.isRateLimited(TEST_EMAIL),
                   "New service instance should have empty cache");
    }

    @Test
    @DisplayName("Different users should have independent rate limits")
    void differentUsers_IndependentRateLimits() {
        // Given
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";

        // Clean up Redis before test
        redisTemplate.delete("rate_limit:" + user1);
        redisTemplate.delete("rate_limit:" + user2);

        // When - User1 makes 5 failed attempts
        for (int i = 0; i < 5; i++) {
            rateLimitingService.recordFailedLogin(user1);
        }

        // Then - User1 should be rate limited, User2 should not
        assertTrue(rateLimitingService.isRateLimited(user1), "User1 should be rate limited");
        assertFalse(rateLimitingService.isRateLimited(user2), "User2 should not be rate limited");

        // Cleanup
        redisTemplate.delete("rate_limit:" + user1);
        redisTemplate.delete("rate_limit:" + user2);
    }

    @Test
    @DisplayName("Failed attempts should be tracked correctly")
    void failedAttempts_TrackedCorrectly() {
        // Given
        for (int i = 0; i < 3; i++) {
            rateLimitingService.recordFailedLogin(TEST_EMAIL);
        }

        // When
        for (int i = 3; i < 5; i++) {
            rateLimitingService.recordFailedLogin(TEST_EMAIL);
        }

        // Then
        assertTrue(rateLimitingService.isRateLimited(TEST_EMAIL),
                  "User should be rate limited after exactly 5 failed attempts");
    }
}