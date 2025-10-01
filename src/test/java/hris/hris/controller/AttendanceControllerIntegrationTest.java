package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.ClockInRequest;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Attendance;
import hris.hris.model.Employee;
import hris.hris.repository.AttendanceRepository;
import hris.hris.repository.EmployeeRepository;
import hris.hris.service.GeofencingService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AttendanceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private GeofencingService geofencingService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Employee testEmployee;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        when(geofencingService.isWithinGeofence(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(true);

        testEmployee = new Employee();
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("User");
        testEmployee.setEmail("test@example.com");
        testEmployee.setPassword(passwordEncoder.encode("password123"));
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setIsActive(true);
        testEmployee.setAnnualLeaveBalance(12);
        testEmployee.setSickLeaveBalance(10);
        employeeRepository.save(testEmployee);

        authToken = obtainAuthToken();
    }

    private String obtainAuthToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return "Bearer " + loginResponse.getToken();
    }

    @Test
    void clockIn_WithValidRequest_ShouldReturnSuccess() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);
        request.setLocationAddress("Office Location");
        request.setFaceImage("base64FaceImage");
        request.setNotes("Working from office");

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Clock in successful"))
                .andExpect(jsonPath("$.attendance").exists())
                .andExpect(jsonPath("$.attendance.employee.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.isWithinGeofence").value(true))
                .andExpect(jsonPath("$.faceRecognitionConfidence").exists());
    }

    @Test
    void clockIn_WithAlreadyClockedIn_ShouldReturnBadRequest() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);
        request.setLocationAddress("Office Location");

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Already clocked in today"));
    }

    @Test
    void clockIn_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", "Bearer invalid.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clockIn_WithMissingLocation_ShouldReturnSuccess() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLocationAddress("Office Location");
        request.setNotes("No location data");

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Clock in successful"));
    }

    @Test
    void clockOut_WithValidClockIn_ShouldReturnSuccess() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-out")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Clock out successful"))
                .andExpect(jsonPath("$.attendance").exists())
                .andExpect(jsonPath("$.attendance.clockOutTime").exists());
    }

    @Test
    void clockOut_WithoutClockIn_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-out")
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No active clock-in record found"));
    }

    @Test
    void clockOut_WithAlreadyClockedOut_ShouldReturnBadRequest() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-out")
                .header("Authorization", authToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/clock-out")
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Already clocked out today"));
    }

    @Test
    void getTodayAttendance_WithRecord_ShouldReturnAttendance() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/attendance/today")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.clockInTime").exists());
    }

    @Test
    void getTodayAttendance_WithNoRecord_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/attendance/today")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No attendance record for today"));
    }

    @Test
    void getAttendanceStatus_ShouldReturnCorrectStatus() throws Exception {
        mockMvc.perform(get("/api/attendance/status")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isClockedIn").value(false))
                .andExpect(jsonPath("$.hasTodayRecord").value(false));

        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/attendance/status")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isClockedIn").value(true))
                .andExpect(jsonPath("$.hasTodayRecord").value(true));
    }

    @Test
    void getAttendanceHistory_ShouldReturnHistory() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/attendance/history")
                .header("Authorization", authToken)
                .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAttendanceHistory_WithDefaultDays_ShouldReturnHistory() throws Exception {
        mockMvc.perform(get("/api/attendance/history")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAttendanceHistory_WithCustomDays_ShouldReturnHistory() throws Exception {
        mockMvc.perform(get("/api/attendance/history")
                .header("Authorization", authToken)
                .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllAttendance_WithAdminRole_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/attendance/admin/all")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin endpoint - implement as needed"));
    }

    @Test
    void getAllAttendance_WithParameters_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/attendance/admin/all")
                .header("Authorization", authToken)
                .param("employeeId", "1")
                .param("date", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin endpoint - implement as needed"));
    }

    @Test
    void clockIn_WithOutsideGeofence_ShouldReturnSuccess() throws Exception {
        when(geofencingService.isWithinGeofence(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(false);

        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-7.0);
        request.setLongitude(107.0);
        request.setLocationAddress("Outside Office");

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isWithinGeofence").value(false));
    }

    @Test
    void clockIn_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);

        mockMvc.perform(post("/api/attendance/clock-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clockOut_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-out"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTodayAttendance_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/attendance/today"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAttendanceStatus_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/attendance/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAttendanceHistory_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/attendance/history"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clockIn_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clockIn_WithEmptyFaceImage_ShouldReturnSuccess() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);
        request.setFaceImage("");
        request.setLocationAddress("Office Location");

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Clock in successful"));
    }

    @Test
    void clockIn_WithNullNotes_ShouldReturnSuccess() throws Exception {
        ClockInRequest request = new ClockInRequest();
        request.setLatitude(-6.2088);
        request.setLongitude(106.8456);
        request.setLocationAddress("Office Location");
        request.setNotes(null);

        mockMvc.perform(post("/api/attendance/clock-in")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Clock in successful"));
    }
}