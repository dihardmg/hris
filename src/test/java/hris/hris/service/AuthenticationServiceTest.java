package hris.hris.service;

import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import hris.hris.security.CustomUserDetailsService;
import hris.hris.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private LoginRequest loginRequest;
    private Employee employee;
    private UserDetails userDetails;
    private Authentication authentication;
    private String token = "test.jwt.token";

    // Set a default JWT expiration for tests
    private Long jwtExpiration = 3600000L; // 1 hour in milliseconds

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("test@example.com");

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com")
                .password("password")
                .authorities("USER")
                .build();

        authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);

        // Set the jwtExpiration field using reflection
        try {
            Field jwtExpirationField = AuthenticationService.class.getDeclaredField("jwtExpiration");
            jwtExpirationField.setAccessible(true);
            jwtExpirationField.set(authenticationService, jwtExpiration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set jwtExpiration field", e);
        }
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnLoginResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(employeeRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(employee));
        when(jwtUtil.generateToken("test@example.com", 1L, "EMP001"))
                .thenReturn(token);

        LoginResponse response = authenticationService.authenticateUser(loginRequest);

        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals("Bearer", response.getType());
        assertNotNull(response.getExpiresAt());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmail("test@example.com");
        verify(jwtUtil).generateToken("test@example.com", 1L, "EMP001");
    }

    @Test
    void authenticateUser_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticateUser(loginRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any(), anyString());
    }

    @Test
    void authenticateUser_WithNonExistentEmployee_ShouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(employeeRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticateUser(loginRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmail("test@example.com");
        verify(jwtUtil, never()).generateToken(anyString(), any(), anyString());
    }

    @Test
    void authenticateUser_WithGeneralException_ShouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Database error"));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticateUser(loginRequest)
        );

        assertEquals("Authentication failed", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any(), anyString());
    }

    @Test
    void getCurrentEmployee_WithExistingEmail_ShouldReturnEmployee() {
        when(employeeRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(employee));

        Employee result = authenticationService.getCurrentEmployee("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());

        verify(employeeRepository).findByEmail("test@example.com");
    }

    @Test
    void getCurrentEmployee_WithNonExistentEmail_ShouldThrowRuntimeException() {
        when(employeeRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authenticationService.getCurrentEmployee("nonexistent@example.com")
        );

        assertEquals("Employee not found", exception.getMessage());

        verify(employeeRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        when(jwtUtil.validateToken(token)).thenReturn(true);

        Boolean result = authenticationService.validateToken(token);

        assertTrue(result);
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        when(jwtUtil.validateToken(token)).thenReturn(false);

        Boolean result = authenticationService.validateToken(token);

        assertFalse(result);
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void getEmailFromToken_WithValidToken_ShouldReturnEmail() {
        when(jwtUtil.getEmailFromToken(token)).thenReturn("test@example.com");

        String result = authenticationService.getEmailFromToken(token);

        assertEquals("test@example.com", result);
        verify(jwtUtil).getEmailFromToken(token);
    }
}