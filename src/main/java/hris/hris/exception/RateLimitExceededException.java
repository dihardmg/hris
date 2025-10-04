package hris.hris.exception;

import hris.hris.dto.RateLimitResponse;

public class RateLimitExceededException extends RuntimeException {

    private final RateLimitResponse rateLimitResponse;
    private final String email;
    private final String clientIP;

    public RateLimitExceededException(String message) {
        super(message);
        this.rateLimitResponse = null;
        this.email = null;
        this.clientIP = null;
    }

    public RateLimitExceededException(String message, RateLimitResponse rateLimitResponse) {
        super(message);
        this.rateLimitResponse = rateLimitResponse;
        this.email = null;
        this.clientIP = null;
    }

    public RateLimitExceededException(String message, String email, String clientIP, Long retryAfterSeconds) {
        super(message);
        this.email = email;
        this.clientIP = clientIP;
        this.rateLimitResponse = RateLimitResponse.loginRateLimit(retryAfterSeconds);
    }

    // public RateLimitExceededException(String message, String email, Long retryAfterSeconds) {
    //     super(message);
    //     this.email = email;
    //     this.clientIP = null;
    //     this.rateLimitResponse = RateLimitResponse.loginRateLimit(retryAfterSeconds);
    // }

    public RateLimitExceededException(String errorType, String message, String resourceType, Long retryAfterSeconds, String email, String clientIP) {
        super(message);
        this.email = email;
        this.clientIP = clientIP;

        switch (resourceType) {
            case "LOGIN":
                this.rateLimitResponse = RateLimitResponse.loginRateLimit(retryAfterSeconds);
                break;
            case "PASSWORD_RESET":
                this.rateLimitResponse = RateLimitResponse.passwordResetRateLimit(retryAfterSeconds);
                break;
            default:
                this.rateLimitResponse = new RateLimitResponse(errorType, message, retryAfterSeconds);
        }
    }

    public RateLimitResponse getRateLimitResponse() {
        return rateLimitResponse;
    }

    public String getEmail() {
        return email;
    }

    public String getClientIP() {
        return clientIP;
    }

    public Long getRetryAfterSeconds() {
        return rateLimitResponse != null ? rateLimitResponse.getRetryAfterSeconds() : null;
    }

    public String getErrorCode() {
        return rateLimitResponse != null ? rateLimitResponse.getErrorCode() : null;
    }

    public RateLimitExceededException withRequestId(String requestId) {
        if (this.rateLimitResponse != null) {
            this.rateLimitResponse.setRequestId(requestId);
        }
        return this;
    }
}