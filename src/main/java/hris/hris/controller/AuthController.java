package hris.hris.controller;

import hris.hris.dto.ApiResponse;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.exception.RateLimitExceededException;
import hris.hris.service.AuthenticationService;
import hris.hris.service.RateLimitingService;
import java.util.HashMap;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();

        if (rateLimitingService.isRateLimited(email)) {
            log.warn("Rate limit exceeded for user: {}", email);

            // Get remaining time
            long retryAfterSeconds = rateLimitingService.getRateLimitTTL(email);
            String clientIP = getClientIP();

            // Generate request ID for tracking
            String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

            // Throw proper exception with detailed information
            throw new RateLimitExceededException(
                "Too many login attempts. For security purposes, your account has been temporarily locked.",
                email,
                clientIP,
                retryAfterSeconds > 0 ? retryAfterSeconds : 300L // Default 5 minutes
            ).withRequestId(requestId);
        }

        try {
            LoginResponse response = authenticationService.authenticateUser(loginRequest);
            rateLimitingService.recordSuccessfulLogin(email);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception e) {
            rateLimitingService.recordFailedLogin(email);
            throw e;
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(createErrorResponse(
                401,
                "Unauthorized",
                "Missing or invalid authorization header format. Please provide a valid Bearer token.",
                "/api/auth/validate"
            ));
        }

        String jwtToken = token.substring(7);
        if (authenticationService.validateToken(jwtToken)) {
            return ResponseEntity.ok(ApiResponse.success("Token is valid"));
        }

        return ResponseEntity.status(401).body(createErrorResponse(
            401,
            "Unauthorized",
            "Invalid or expired token. Please authenticate again.",
            "/api/auth/validate"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(createErrorResponse(
                401,
                "Unauthorized",
                "Missing or invalid authorization header format. Please provide a valid Bearer token.",
                "/api/auth/me"
            ));
        }

        String jwtToken = token.substring(7);
        if (authenticationService.validateToken(jwtToken)) {
            String email = authenticationService.getEmailFromToken(jwtToken);
            var employee = authenticationService.getCurrentEmployee(email);
            return ResponseEntity.ok(ApiResponse.success(employee));
        }

        return ResponseEntity.status(401).body(createErrorResponse(
            401,
            "Unauthorized",
            "Invalid or expired token. Please authenticate again.",
            "/api/auth/me"
        ));
    }

    @GetMapping("/debug/hash")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
            put("password", password);
            put("hash", hash);
        }});
    }

    @PostMapping("/debug/reset-rate-limit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetRateLimit(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // Debug information before reset
        boolean wasLimited = rateLimitingService.isUserRateLimited(email);
        long ttlBefore = rateLimitingService.getRateLimitTTL(email);

        // Reset rate limit
        rateLimitingService.resetRateLimit(email);

        // Debug information after reset
        boolean isLimitedAfter = rateLimitingService.isUserRateLimited(email);
        long ttlAfter = rateLimitingService.getRateLimitTTL(email);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("email", email);
        response.put("wasRateLimited", wasLimited);
        response.put("ttlBeforeReset", ttlBefore);
        response.put("isLimitedAfterReset", isLimitedAfter);
        response.put("ttlAfterReset", ttlAfter);
        response.put("resetSuccessful", !isLimitedAfter);

        log.info("Rate limit reset requested for email: {}, wasLimited: {}, resetSuccessful: {}",
                email, wasLimited, !isLimitedAfter);

        return ResponseEntity.ok(ApiResponse.success(response, "Rate limit reset successful"));
    }

    @GetMapping("/debug/rate-limit-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRateLimitStatus(@RequestParam String email) {
        boolean isLimited = rateLimitingService.isUserRateLimited(email);
        long ttl = rateLimitingService.getRateLimitTTL(email);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("email", email);
        response.put("isRateLimited", isLimited);
        response.put("remainingTimeSeconds", ttl);
        response.put("remainingTimeMinutes", ttl > 0 ? ttl / 60 : 0);
        response.put("isCurrentlyLocked", isLimited && ttl > 0);

        return ResponseEntity.ok(ApiResponse.success(response, "Rate limit status retrieved"));
    }

    @PostMapping("/debug/verify")
    public ResponseEntity<?> verifyHash(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("password", password);
            put("hash", hash);
            put("matches", matches);
        }});
    }

    private String getClientIP() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = httpServletRequest.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }

        return httpServletRequest.getRemoteAddr();
    }

    private Map<String, Object> createErrorResponse(int status, String error, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
        errorResponse.put("status", status);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }
}