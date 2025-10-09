package hris.hris.service;

import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import hris.hris.security.CustomUserDetailsService;
import hris.hris.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@Slf4j
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest, String clientIP) {
        try {
            log.info("Attempting authentication for user: {} from IP: {}", loginRequest.getEmail(), clientIP);

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Employee employee = employeeRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Employee not found"));

            String token = jwtUtil.generateToken(
                employee.getEmail(),
                employee.getId(),
                employee.getEmployeeId()
            );

            log.info("Authentication successful for user: {}", loginRequest.getEmail());

            // Record successful login for rate limiting
            rateLimitingService.recordSuccessfulLogin(loginRequest.getEmail(), clientIP);

            // Calculate token expiration time
            Date expiresAt = new Date(System.currentTimeMillis() + jwtExpiration);

            return LoginResponse.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        } catch (Exception e) {
            log.error("Authentication error for user: {}", loginRequest.getEmail(), e);
            throw new BadCredentialsException("Authentication failed");
        }
    }

    @Transactional(readOnly = true)
    public Employee getCurrentEmployee(String email) {
        return employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public Boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String getEmailFromToken(String token) {
        return jwtUtil.getEmailFromToken(token);
    }
}