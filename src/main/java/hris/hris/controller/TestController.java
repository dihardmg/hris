package hris.hris.controller;

import hris.hris.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String to) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Testing email sending to: {}", to);
            emailService.sendPasswordResetEmail(to, "test-token-123456");

            response.put("success", true);
            response.put("message", "Test email sent successfully to: " + to);
            log.info("Test email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send test email: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to send test email: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }

        return ResponseEntity.ok(response);
    }
}