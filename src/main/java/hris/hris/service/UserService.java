package hris.hris.service;

import hris.hris.dto.PasswordUpdateRequest;
import hris.hris.exception.PasswordUpdateException;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        log.info("Starting password update process for user: {}", email);

        try {
            // Find user by email
            Employee user = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for email: {}", email);
                    return new PasswordUpdateException("User not found",
                        PasswordUpdateException.PasswordUpdateErrorType.USER_NOT_FOUND);
                });

            // Validate password matching
            if (!request.isPasswordMatching()) {
                log.warn("Password confirmation does not match for user: {}", email);
                throw new PasswordUpdateException("New password and confirmation must match",
                    PasswordUpdateException.PasswordUpdateErrorType.PASSWORDS_DO_NOT_MATCH);
            }

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                log.warn("Current password verification failed for user: {}", email);
                throw new PasswordUpdateException("Current password is incorrect",
                    PasswordUpdateException.PasswordUpdateErrorType.INVALID_CURRENT_PASSWORD);
            }

            // Check if new password is same as current password
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                log.warn("New password is same as current password for user: {}", email);
                throw new PasswordUpdateException("New password must be different from current password",
                    PasswordUpdateException.PasswordUpdateErrorType.SAME_PASSWORD_AS_CURRENT);
            }

            // Update password with encrypted new password
            String oldPasswordHash = user.getPassword();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            employeeRepository.save(user);

            log.info("Password updated successfully for user: {}", email);

        } catch (PasswordUpdateException ex) {
            throw ex; // Re-throw our custom exceptions
        } catch (Exception ex) {
            log.error("Unexpected error during password update for user: {}", email, ex);
            throw new PasswordUpdateException("Password update failed due to system error",
                PasswordUpdateException.PasswordUpdateErrorType.VALIDATION_FAILED, ex);
        }
    }
}