package hris.hris.exception;

import hris.hris.dto.ApiResponse;
import hris.hris.dto.RateLimitResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtTokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleJwtTokenExpiredException(
            JwtTokenExpiredException ex, WebRequest request) {
        log.warn("JWT token expired: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", "Invalid email or password");
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.error("Authentication error: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Authentication Failed");
        response.put("message", "Authentication failed");
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Access Denied");
        response.put("message", "You don't have permission to access this resource");
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleRateLimitExceededException(
            RateLimitExceededException ex, WebRequest request) {

        log.warn("Rate limit exceeded for email: {}, IP: {}, Message: {}",
                ex.getEmail(), ex.getClientIP(), ex.getMessage());

        RateLimitResponse rateLimitResponse = ex.getRateLimitResponse();

        // Create response headers following RESTful best practices
        HttpHeaders headers = new HttpHeaders();

        if (rateLimitResponse != null) {
            // Standard HTTP headers for rate limiting
            if (rateLimitResponse.getRetryAfterSeconds() != null) {
                headers.add("Retry-After", rateLimitResponse.getRetryAfterSeconds().toString());
            }

            if (rateLimitResponse.getMaxAttempts() != null) {
                headers.add("X-RateLimit-Limit", rateLimitResponse.getMaxAttempts().toString());
            }

            if (rateLimitResponse.getRemainingAttempts() != null) {
                headers.add("X-RateLimit-Remaining", rateLimitResponse.getRemainingAttempts().toString());
            }

            if (rateLimitResponse.getCurrentAttempts() != null) {
                headers.add("X-RateLimit-Used", rateLimitResponse.getCurrentAttempts().toString());
            }

            if (rateLimitResponse.getResetTimeUnix() != null) {
                headers.add("X-RateLimit-Reset", rateLimitResponse.getResetTimeUnix().toString());
            }

            // Custom headers for additional context
            headers.add("X-RateLimit-Resource", rateLimitResponse.getResourceType());
            headers.add("X-RateLimit-Window", rateLimitResponse.getWindowType());
            headers.add("X-RateLimit-Error-Code", rateLimitResponse.getErrorCode());

            if (rateLimitResponse.getRequestId() != null) {
                headers.add("X-Request-ID", rateLimitResponse.getRequestId());
            }
        }

        // Return enhanced rate limit response
        ApiResponse<RateLimitResponse> response = ApiResponse.error(rateLimitResponse);

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(response);
    }

    @ExceptionHandler(LoginSuccessRateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleLoginSuccessRateLimitException(
            LoginSuccessRateLimitException ex, WebRequest request) {

        log.warn("Success login rate limit exceeded for email: {}, IP: {}, Message: {}",
                ex.getEmail(), ex.getClientIP(), ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("error", "Too Many Requests");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        response.put("retryAfter", ex.getRetryAfterSeconds());
        response.put("email", ex.getEmail());
        response.put("clientIP", ex.getClientIP());
        response.put("rateLimitType", "LOGIN_SUCCESS");

        if (ex.getRequestId() != null) {
            response.put("requestId", ex.getRequestId());
        }

        // Create response headers following RESTful best practices
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        headers.add("X-RateLimit-Resource", "login_success");
        headers.add("X-RateLimit-Error-Code", "LOGIN_SUCCESS_EXCEEDED");

        if (ex.getRequestId() != null) {
            headers.add("X-Request-ID", ex.getRequestId());
        }

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(headers)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, Object> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            // If field already has errors, add to list
            if (errors.containsKey(fieldName)) {
                Object existingError = errors.get(fieldName);
                if (existingError instanceof java.util.List) {
                    ((java.util.List<String>) existingError).add(errorMessage);
                } else {
                    java.util.List<String> errorList = new java.util.ArrayList<>();
                    errorList.add((String) existingError);
                    errorList.add(errorMessage);
                    errors.put(fieldName, errorList);
                }
            } else {
                // Create single error list for each field
                java.util.List<String> errorList = new java.util.ArrayList<>();
                errorList.add(errorMessage);
                errors.put(fieldName, errorList);
            }
        });

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", "400");
        response.put("status", "BAD_REQUEST");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        log.error("Runtime error: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(LeaveRequestException.class)
    public ResponseEntity<Map<String, Object>> handleLeaveRequestException(
            LeaveRequestException ex, WebRequest request) {
        log.warn("Leave request business error: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        // Add error code and details if available
        if (ex.getErrorCode() != null) {
            response.put("errorCode", ex.getErrorCode());
        }
        if (ex.getDetails() != null) {
            response.put("details", ex.getDetails());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(LeaveRequestValidationException.class)
    public ResponseEntity<Map<String, Object>> handleLeaveRequestValidationException(
            LeaveRequestValidationException ex, WebRequest request) {
        log.warn("Leave request validation error: {}", ex.getMessage());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", "400");
        response.put("status", "BAD_REQUEST");
        response.put("errors", ex.getErrors());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AttendanceException.class)
    public ResponseEntity<Map<String, Object>> handleAttendanceException(
            AttendanceException ex, WebRequest request) {
        log.warn("Attendance business error: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        // Add error code and details if available
        if (ex.getErrorCode() != null) {
            response.put("errorCode", ex.getErrorCode());
        }
        if (ex.getDetails() != null) {
            response.put("details", ex.getDetails());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.warn("Business logic error: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        response.put("error", "Unprocessable Entity");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        // Add error code and details if available
        if (ex.getErrorCode() != null) {
            response.put("errorCode", ex.getErrorCode());
        }
        if (ex.getDetails() != null) {
            response.put("details", ex.getDetails());
        }

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}