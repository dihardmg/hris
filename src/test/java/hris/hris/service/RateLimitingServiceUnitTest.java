package hris.hris.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingServiceUnitTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RateLimitingService rateLimitingService;

    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("New user should not be rate limited")
    void isNewUser_NotRateLimited() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean isLimited = rateLimitingService.isRateLimited(TEST_EMAIL);

        // Then
        assertFalse(isLimited, "New user should not be rate limited");
        verify(valueOperations).get("rate_limit:" + TEST_EMAIL);
    }

    @Test
    @DisplayName("After 5 failed attempts, user should be rate limited")
    void afterFiveFailedAttempts_IsRateLimited() {
        // Given - Mock JSON response for locked user
        String lockedUserJson = "{\"failedAttempts\":5,\"locked\":true,\"lockEndTime\":\"" +
                                LocalDateTime.now().plusMinutes(5) + "\"}";
        when(valueOperations.get("rate_limit:" + TEST_EMAIL)).thenReturn(lockedUserJson);

        // When
        boolean isLimited = rateLimitingService.isRateLimited(TEST_EMAIL);

        // Then
        assertTrue(isLimited, "User with 5 failed attempts should be rate limited");
        verify(valueOperations).get("rate_limit:" + TEST_EMAIL);
    }

    @Test
    @DisplayName("Failed login should store data in Redis")
    void recordFailedLogin_ShouldStoreInRedis() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        rateLimitingService.recordFailedLogin(TEST_EMAIL);

        // Then
        verify(valueOperations).set(eq("rate_limit:" + TEST_EMAIL), anyString(), eq(600L), any());
    }

    @Test
    @DisplayName("Successful login should delete Redis entry")
    void successfulLogin_ShouldDeleteRedisEntry() {
        // When
        rateLimitingService.recordSuccessfulLogin(TEST_EMAIL);

        // Then
        verify(redisTemplate).delete("rate_limit:" + TEST_EMAIL);
    }

    @Test
    @DisplayName("Different users should have independent rate limits")
    void differentUsers_IndependentRateLimits() {
        // Given
        String user1 = "user1@example.com";
        String user2 = "user2@example.com";

        String lockedUserJson = "{\"failedAttempts\":5,\"locked\":true,\"lockEndTime\":\"" +
                                LocalDateTime.now().plusMinutes(5) + "\"}";

        when(valueOperations.get("rate_limit:" + user1)).thenReturn(lockedUserJson);
        when(valueOperations.get("rate_limit:" + user2)).thenReturn(null);

        // When
        boolean user1Limited = rateLimitingService.isRateLimited(user1);
        boolean user2Limited = rateLimitingService.isRateLimited(user2);

        // Then
        assertTrue(user1Limited, "User1 should be rate limited");
        assertFalse(user2Limited, "User2 should not be rate limited");

        verify(valueOperations).get("rate_limit:" + user1);
        verify(valueOperations).get("rate_limit:" + user2);
    }

    @Test
    @DisplayName("Expired lock should not be enforced")
    void expiredLock_ShouldNotBeEnforced() {
        // Given - User is locked but lock time has expired
        String expiredLockJson = "{\"failedAttempts\":5,\"locked\":true,\"lockEndTime\":\"" +
                                LocalDateTime.now().minusMinutes(1) + "\"}";
        when(valueOperations.get("rate_limit:" + TEST_EMAIL)).thenReturn(expiredLockJson);

        // When
        boolean isLimited = rateLimitingService.isRateLimited(TEST_EMAIL);

        // Then
        assertFalse(isLimited, "User with expired lock should not be rate limited");
    }
}