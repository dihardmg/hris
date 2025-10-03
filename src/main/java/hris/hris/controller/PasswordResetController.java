package hris.hris.controller;

import hris.hris.dto.PasswordResetConfirmRequest;
import hris.hris.dto.PasswordResetRequest;
import hris.hris.dto.PasswordResetResponse;
import hris.hris.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        log.info("Password reset request received from IP: {} for email: {}", clientIp, request.getEmail());

        try {
            PasswordResetResponse response = passwordResetService.requestPasswordReset(request, clientIp);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing password reset request for email: {}", request.getEmail(), e);
            // Return generic success message to prevent email enumeration
            PasswordResetResponse response = PasswordResetResponse.builder()
                    .message("If an account with this email exists, a password reset link has been sent.")
                    .expiresIn("1 hour")
                    .success(true)
                    .build();
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        log.info("Password reset confirmation received from IP: {}", clientIp);

        try {
            PasswordResetResponse response = passwordResetService.resetPassword(request, clientIp);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid password reset request: {}", e.getMessage());
            PasswordResetResponse response = PasswordResetResponse.builder()
                    .message(e.getMessage())
                    .success(false)
                    .build();
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Error processing password reset confirmation", e);
            PasswordResetResponse response = PasswordResetResponse.builder()
                    .message("An error occurred while resetting your password. Please try again.")
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyResetToken(@RequestParam String token) {
        log.info("Token verification request received");

        try {
            boolean isValid = passwordResetService.validateResetToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Token is valid" : "Token is invalid or expired");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error verifying reset token", e);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Error verifying token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Fallback methods for rate limiting
    public ResponseEntity<PasswordResetResponse> handlePasswordResetRateLimit(
            PasswordResetRequest request, HttpServletRequest httpRequest, Exception e) {

        log.warn("Password reset request rate limited for IP: {}", getClientIp(httpRequest));

        PasswordResetResponse response = PasswordResetResponse.builder()
                .message("Too many password reset requests. Please try again later.")
                .success(false)
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}