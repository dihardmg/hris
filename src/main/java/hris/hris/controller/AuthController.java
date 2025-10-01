package hris.hris.controller;

import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.service.AuthenticationService;
import hris.hris.service.RateLimitingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();

        if (rateLimitingService.isRateLimited(email)) {
            log.warn("Rate limit exceeded for user: {}", email);
            return ResponseEntity.status(429).body(null);
        }

        try {
            LoginResponse response = authenticationService.authenticateUser(loginRequest);
            rateLimitingService.recordSuccessfulLogin(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            rateLimitingService.recordFailedLogin(email);
            throw e;
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            if (authenticationService.validateToken(jwtToken)) {
                return ResponseEntity.ok("Token is valid");
            }
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            if (authenticationService.validateToken(jwtToken)) {
                String email = authenticationService.getEmailFromToken(jwtToken);
                var employee = authenticationService.getCurrentEmployee(email);
                return ResponseEntity.ok(employee);
            }
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }

    @GetMapping("/debug/hash")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
            put("password", password);
            put("hash", hash);
        }});
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
}