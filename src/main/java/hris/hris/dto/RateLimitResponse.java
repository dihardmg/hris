package hris.hris.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RateLimitResponse {

    private String error;
    private String errorType;
    private String errorCode;
    private String message;

    // Rate limiting specific information
    private Long retryAfterSeconds;
    private Long retryAfterMinutes;
    private LocalDateTime retryAfterDateTime;
    private LocalDateTime lockEndTime;

    // Rate limit metadata
    private Integer maxAttempts;
    private Integer currentAttempts;
    private Integer remainingAttempts;
    private Long resetTimeUnix;
    private LocalDateTime resetTime;

    // Additional context
    private String resourceType; // LOGIN, PASSWORD_RESET, API_CALL
    private String windowType; // PER_MINUTE, PER_HOUR, PER_DAY
    private String nextAction; // WAIT, RESET_PASSWORD, CONTACT_SUPPORT

    // Security context (for debugging)
    private String requestId;
    private LocalDateTime timestamp;

    public RateLimitResponse(String errorType, String message, Long retryAfterSeconds) {
        this.errorType = errorType;
        this.error = errorType;
        this.message = message;
        this.retryAfterSeconds = retryAfterSeconds;
        this.retryAfterMinutes = retryAfterSeconds != null ? retryAfterSeconds / 60 : null;
        this.timestamp = LocalDateTime.now();

        if (retryAfterSeconds != null) {
            this.retryAfterDateTime = LocalDateTime.now().plusSeconds(retryAfterSeconds);
            this.lockEndTime = this.retryAfterDateTime;
        }

        // Auto-generate error code
        this.errorCode = generateErrorCode(errorType);
    }

    public static RateLimitResponse loginRateLimit(Long retryAfterSeconds) {
        RateLimitResponse response = new RateLimitResponse(
            "LOGIN_RATE_LIMIT_EXCEEDED",
            "Too many login attempts. For your security, account has been temporarily locked.",
            retryAfterSeconds
        );

        response.setMaxAttempts(5);
        response.setCurrentAttempts(5); // User has exceeded the limit
        response.setRemainingAttempts(0);
        response.setResetTimeUnix(retryAfterSeconds);
        response.setResourceType("LOGIN");
        response.setWindowType("PER_SESSION");
        response.setNextAction("WAIT");

        return response;
    }

    public static RateLimitResponse passwordResetRateLimit(Long retryAfterSeconds) {
        RateLimitResponse response = new RateLimitResponse(
            "PASSWORD_RESET_RATE_LIMIT_EXCEEDED",
            "Too many password reset requests. Please wait before requesting another reset.",
            retryAfterSeconds
        );

        response.setMaxAttempts(3);
        response.setResourceType("PASSWORD_RESET");
        response.setWindowType("PER_HOUR");
        response.setNextAction("WAIT");

        return response;
    }

    public static RateLimitResponse apiRateLimit(Integer maxRequests, Long resetTime) {
        RateLimitResponse response = new RateLimitResponse(
            "API_RATE_LIMIT_EXCEEDED",
            "API rate limit exceeded. Please reduce request frequency.",
            resetTime
        );

        response.setMaxAttempts(maxRequests);
        response.setResourceType("API_CALL");
        response.setWindowType("PER_MINUTE");
        response.setResetTimeUnix(resetTime);
        response.setNextAction("WAIT");

        return response;
    }

    private String generateErrorCode(String errorType) {
        return "RATE_" + errorType.toUpperCase().replace(" ", "_");
    }

    public RateLimitResponse withCurrentAttempts(Integer attempts) {
        this.currentAttempts = attempts;
        if (this.maxAttempts != null) {
            this.remainingAttempts = Math.max(0, this.maxAttempts - attempts);
        }
        return this;
    }

    public RateLimitResponse withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public RateLimitResponse withResetTime(Long resetTime) {
        this.resetTimeUnix = resetTime;
        if (resetTime != null) {
            this.resetTime = LocalDateTime.now().plusSeconds(resetTime);
        }
        return this;
    }
}