package hris.hris.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String SECRET = "testSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm";
    private static final Long EXPIRATION = 3600000L; // 1 hour
    private static final String EMAIL = "test@example.com";
    private static final Long EMPLOYEE_ID = 123L;
    private static final String EMPLOYEE_CODE = "EMP001";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        String extractedEmail = jwtUtil.getEmailFromToken(token);

        assertEquals(EMAIL, extractedEmail);
    }

    @Test
    void getEmployeeIdFromToken_ShouldReturnCorrectEmployeeId() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        Long extractedEmployeeId = jwtUtil.getEmployeeIdFromToken(token);

        assertEquals(EMPLOYEE_ID, extractedEmployeeId);
    }

    @Test
    void getEmployeeCodeFromToken_ShouldReturnCorrectEmployeeCode() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        String extractedEmployeeCode = jwtUtil.getEmployeeCodeFromToken(token);

        assertEquals(EMPLOYEE_CODE, extractedEmployeeCode);
    }

    @Test
    void getExpirationDateFromToken_ShouldReturnFutureDate() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        Date now = new Date();

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(now));
        assertTrue(expirationDate.getTime() - now.getTime() < EXPIRATION);
    }

    @Test
    void isTokenExpired_ShouldReturnFalseForValidToken() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void validateToken_WithCorrectEmail_ShouldReturnTrue() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        assertTrue(jwtUtil.validateToken(token, EMAIL));
    }

    @Test
    void validateToken_WithIncorrectEmail_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);
        String wrongEmail = "wrong@example.com";

        assertFalse(jwtUtil.validateToken(token, wrongEmail));
    }

    @Test
    void validateToken_WithoutEmail_ShouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        Long shortExpiration = 1L; // 1 millisecond
        ReflectionTestUtils.setField(jwtUtil, "expiration", shortExpiration);

        String token = jwtUtil.generateToken(EMAIL, EMPLOYEE_ID, EMPLOYEE_CODE);

        try {
            Thread.sleep(10); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void getEmailFromToken_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";

        assertThrows(JwtException.class, () -> jwtUtil.getEmailFromToken(invalidToken));
    }

    @Test
    void getEmployeeIdFromToken_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";

        assertThrows(JwtException.class, () -> jwtUtil.getEmployeeIdFromToken(invalidToken));
    }

    @Test
    void getEmployeeCodeFromToken_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";

        assertThrows(JwtException.class, () -> jwtUtil.getEmployeeCodeFromToken(invalidToken));
    }
}