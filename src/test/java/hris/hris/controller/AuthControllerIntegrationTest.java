package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import hris.hris.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RateLimitingService rateLimitingService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        when(rateLimitingService.isRateLimited(anyString())).thenReturn(false);

        testEmployee = new Employee();
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("User");
        testEmployee.setEmail("test@example.com");
        testEmployee.setPassword(passwordEncoder.encode("password123"));
        testEmployee.setEmployeeId("AUTH001");
        testEmployee.setPhoneNumber("+1234567890");
        testEmployee.setIsActive(true);
        testEmployee.setAnnualLeaveBalance(12);
        testEmployee.setSickLeaveBalance(10);
        employeeRepository.save(testEmployee);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.employeeCode").value("AUTH001"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_WithNonExistentUser_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_WithRateLimit_ShouldReturnTooManyRequests() throws Exception {
        when(rateLimitingService.isRateLimited("test@example.com")).thenReturn(true);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is(429));
    }

    @Test
    void login_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("invalid-email");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithMissingPassword_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        LoginResponse response = objectMapper.readValue(loginResponse, LoginResponse.class);
        String token = "Bearer " + response.getToken();

        mockMvc.perform(post("/api/auth/validate")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid"));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/validate")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void validateToken_WithMissingBearer_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/validate")
                .header("Authorization", "invalidformat"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid token"));
    }

    @Test
    void validateToken_WithMissingToken_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(post("/api/auth/validate"))
                .andExpect(status().is(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void getCurrentUser_WithValidToken_ShouldReturnUserInfo() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        LoginResponse response = objectMapper.readValue(loginResponse, LoginResponse.class);
        String token = "Bearer " + response.getToken();

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getCurrentUser_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid token"));
    }

    @Test
    void generateHash_ShouldReturnPasswordHash() throws Exception {
        mockMvc.perform(get("/api/auth/debug/hash")
                .param("password", "testpassword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value("testpassword"))
                .andExpect(jsonPath("$.hash").exists())
                .andExpect(jsonPath("$.hash", not(equalTo("testpassword"))));
    }

    @Test
    void verifyHash_WithMatchingPassword_ShouldReturnTrue() throws Exception {
        String password = "testpassword";
        String hash = passwordEncoder.encode(password);

        mockMvc.perform(post("/api/auth/debug/verify")
                .param("password", password)
                .param("hash", hash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value(password))
                .andExpect(jsonPath("$.hash").value(hash))
                .andExpect(jsonPath("$.matches").value(true));
    }

    @Test
    void verifyHash_WithNonMatchingPassword_ShouldReturnFalse() throws Exception {
        String password = "testpassword";
        String wrongPassword = "wrongpassword";
        String hash = passwordEncoder.encode(password);

        mockMvc.perform(post("/api/auth/debug/verify")
                .param("password", wrongPassword)
                .param("hash", hash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value(wrongPassword))
                .andExpect(jsonPath("$.hash").value(hash))
                .andExpect(jsonPath("$.matches").value(false));
    }

    @Test
    void login_WithInactiveEmployee_ShouldReturnUnauthorized() throws Exception {
        testEmployee.setIsActive(false);
        employeeRepository.save(testEmployee);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_WithEmptyEmail_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithShortPassword_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is(401));
    }
}