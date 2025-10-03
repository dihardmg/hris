package hris.hris.service;

import hris.hris.dto.PasswordResetConfirmRequest;
import hris.hris.dto.PasswordResetRequest;
import hris.hris.dto.PasswordResetResponse;
import hris.hris.entity.PasswordHistory;
import hris.hris.entity.PasswordResetToken;
import hris.hris.exception.InvalidTokenException;
import hris.hris.exception.RateLimitExceededException;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import hris.hris.repository.PasswordHistoryRepository;
import hris.hris.repository.PasswordResetTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.token-expiration:3600000}")
    private long tokenExpirationMs; // Default 1 hour

    @Value("${app.password-reset.max-attempts:3}")
    private int maxAttemptsPerHour;

    @Value("${app.password-check.history-limit:5}")
    private int passwordHistoryLimit;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest request, String clientIp) {
        String email = request.getEmail();
        log.info("Password reset request received for email: {} from IP: {}", email, clientIp);

        // Check rate limiting
        checkRateLimit(email);

        // Check if email exists (but don't reveal if it doesn't exist)
        boolean emailExists = employeeRepository.existsByEmail(email);

        if (emailExists) {
            // Invalidate any existing tokens for this email
            tokenRepository.invalidateAllTokensForEmail(email, LocalDateTime.now());

            // Generate new token
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

            PasswordResetToken token = PasswordResetToken.builder()
                    .token(resetToken)
                    .email(email)
                    .expiryDate(expiryDate)
                    .build();

            tokenRepository.save(token);

            // Send email
            try {
                emailService.sendPasswordResetEmail(email, resetToken);
                log.info("Password reset email sent to: {}", email);
            } catch (Exception e) {
                log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
                log.warn("!!! FOR TESTING ONLY - Reset token: {} !!!", resetToken);
                log.warn("!!! Use this link for testing: {}/reset-password?token={} !!!",
                    frontendUrl, resetToken);
                // Continue with success response to prevent email enumeration
            }
        } else {
            log.info("Password reset requested for non-existent email: {}", email);
        }

        // Always return success to prevent email enumeration
        return PasswordResetResponse.builder()
                .message("If an account with this email exists, a password reset link has been sent.")
                .expiresIn("1 hour")
                .success(true)
                .build();
    }

    @Transactional
    public PasswordResetResponse resetPassword(PasswordResetConfirmRequest request, String clientIp) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        log.info("Password reset attempt with token from IP: {}", clientIp);

        // Validate password confirmation
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Find and validate token
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw new InvalidTokenException("Invalid or expired reset token");
        }

        // Find employee
        Employee employee = employeeRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check password history
        if (isPasswordReused(employee.getId(), newPassword)) {
            throw new IllegalArgumentException("You cannot reuse your last 5 passwords. Please choose a different password.");
        }

        // Save current password to history before updating
        savePasswordToHistory(employee);

        // Update password
        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);

        // Mark token as used
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        // Invalidate all other tokens for this email
        tokenRepository.invalidateAllTokensForEmail(employee.getEmail(), LocalDateTime.now());

        // Send confirmation email
        try {
            emailService.sendPasswordResetConfirmationEmail(employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email: {}", e.getMessage());
        }

        log.info("Password reset successful for employee: {} (ID: {})", employee.getEmail(), employee.getId());

        return PasswordResetResponse.builder()
                .message("Password has been successfully reset.")
                .success(true)
                .build();
    }

    @Transactional
    public boolean validateResetToken(String token) {
        return tokenRepository.findByTokenAndUsedFalse(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public void checkRateLimit(String email) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = tokenRepository.countRecentRequestsByEmail(email, oneHourAgo);

        if (recentAttempts >= maxAttemptsPerHour) {
            log.warn("Rate limit exceeded for email: {}. Attempts: {}", email, recentAttempts);
            throw new RateLimitExceededException("Too many password reset requests. Please try again later.");
        }
    }

    private boolean isPasswordReused(Long employeeId, String newPassword) {
        List<PasswordHistory> passwordHistory = passwordHistoryRepository.findLast5PasswordsByEmployeeId(employeeId);

        return passwordHistory.stream()
                .anyMatch(ph -> passwordEncoder.matches(newPassword, ph.getHashedPassword()));
    }

    private void savePasswordToHistory(Employee employee) {
        PasswordHistory passwordHistory = PasswordHistory.builder()
                .employeeId(employee.getId())
                .hashedPassword(employee.getPassword())
                .build();

        passwordHistoryRepository.save(passwordHistory);

        // Clean up old password history (keep only last 10)
        List<PasswordHistory> allHistory = passwordHistoryRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId());
        if (allHistory.size() > 10) {
            List<PasswordHistory> toDelete = allHistory.subList(10, allHistory.size());
            passwordHistoryRepository.deleteAll(toDelete);
        }
    }

    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteExpiredTokens(now);
        log.info("Cleaned up expired password reset tokens");
    }
}