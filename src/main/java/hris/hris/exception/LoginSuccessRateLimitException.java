package hris.hris.exception;

import lombok.Getter;

@Getter
public class LoginSuccessRateLimitException extends RuntimeException {
    private final String email;
    private final String clientIP;
    private final long retryAfterSeconds;
    private final String requestId;

    public LoginSuccessRateLimitException(String message, String email, String clientIP, long retryAfterSeconds) {
        super(message);
        this.email = email;
        this.clientIP = clientIP;
        this.retryAfterSeconds = retryAfterSeconds;
        this.requestId = null;
    }

    public LoginSuccessRateLimitException(String message, String email, String clientIP, long retryAfterSeconds, String requestId) {
        super(message);
        this.email = email;
        this.clientIP = clientIP;
        this.retryAfterSeconds = retryAfterSeconds;
        this.requestId = requestId;
    }

    public LoginSuccessRateLimitException withRequestId(String requestId) {
        return new LoginSuccessRateLimitException(
            this.getMessage(), this.email, this.clientIP, this.retryAfterSeconds, requestId
        );
    }
}