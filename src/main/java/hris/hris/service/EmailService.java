package hris.hris.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@ConditionalOnProperty(name = "spring.mail.host")
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    // Use verified sender email for SendGrid
    private final String verifiedSenderEmail = "dihardmg@gmail.com";

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        if (mailSender == null) {
            log.info("Email service is disabled. Password reset token for {}: {}", toEmail, resetToken);
            return;
        }

        try {
            log.info("Attempting to send password reset email to: {}", toEmail);
            log.info("Mail configuration - Host: {}, Port: {}, Username: {}",
                    mailSender.getClass().getSimpleName(),
                    "587",
                    fromEmail);

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            log.info("Generated reset link: {}", resetLink);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(verifiedSenderEmail);
            helper.setTo(toEmail);
            helper.setSubject("GSJ HRIS - Password Reset Request");

            String htmlContent = buildPasswordResetEmailTemplate(resetLink);
            helper.setText(htmlContent, true);

            log.info("Sending email message...");
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendPasswordResetConfirmationEmail(String toEmail) {
        if (mailSender == null) {
            log.info("Email service is disabled. Password reset confirmation for {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(verifiedSenderEmail);
            message.setTo(toEmail);
            message.setSubject("GSJ HRIS - Password Successfully Reset");
            message.setText("Your password has been successfully reset. If you did not perform this action, please contact IT support immediately.");

            mailSender.send(message);
            log.info("Password reset confirmation email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to {}: {}", toEmail, e.getMessage());
            // Don't throw exception for confirmation emails
        }
    }

    private String buildPasswordResetEmailTemplate(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>GSJ HRIS - Password Reset</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button {
                        display: inline-block;
                        padding: 12px 24px;
                        background-color: #3498db;
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .security-note { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 10px; margin: 15px 0; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>GSJ HRIS</h1>
                        <p>Password Reset Request</p>
                    </div>

                    <div class="content">
                        <p>Hello,</p>
                        <p>We received a request to reset your password for your GSJ HRIS account. Click the button below to reset your password:</p>

                        <div style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </div>

                        <div class="security-note">
                            <strong>Security Notice:</strong>
                            <ul>
                                <li>This link will expire in 1 hour</li>
                                <li>If you didn't request this password reset, please ignore this email</li>
                                <li>Never share this link with anyone</li>
                            </ul>
                        </div>

                        <p>If the button doesn't work, copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; background-color: #e9ecef; padding: 10px; border-radius: 5px;">%s</p>

                        <p>For security reasons, please make sure your new password:</p>
                        <ul>
                            <li>Is at least 6 characters long</li>
                            <li>Contains uppercase and lowercase letters</li>
                            <li>Contains at least one number</li>
                            <li>Contains at least one special character</li>
                        </ul>
                    </div>

                    <div class="footer">
                        <p>&copy; 2025 GSJ HRIS. All rights reserved.</p>
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetLink, resetLink);
    }
}